package io.github.knap43.photomode;

import org.joml.Matrix4f;

import io.github.knap43.photomode.mixin.CameraAccessor;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.Screenshot;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/** Photo mode state. Everything here runs on the render thread except the server freeze. */
public final class PhotoController {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final ResourceLocation DOF_CHAIN =
            ResourceLocation.fromNamespaceAndPath(PhotoMode.MOD_ID, "shaders/post/dof.json");
    /** True isometric elevation: atan(1/√2). */
    private static final float ISO_PITCH = 35.264F;
    private static final float FOCUS_RANGE = 512.0F;

    private static boolean active;
    private static PhotoCamera camera;

    // Restored on exit.
    private static Input savedInput;
    private static boolean savedHideGui;
    private static CameraType savedCameraType;
    private static boolean savedSmartCull;

    // Survive between sessions within a game launch.
    private static boolean showPlayer = true;
    private static boolean dof;
    private static boolean isometric;
    private static boolean freezeWorld = true;
    private static double flySpeed = 6.0; // blocks per second
    private static float isoHalfHeight = 16.0F; // blocks
    private static float dofStrength = 1.0F;

    private static float focus = 8.0F;
    private static boolean pendingPhoto;
    private static long lastFrameNanos;
    private static boolean hintShown;

    /** Touched only on the integrated server thread. */
    private static volatile boolean serverFrozenByUs;

    private PhotoController() {}

    public static boolean active() {
        return active;
    }

    public static boolean hidesPlayer() {
        return active && !showPlayer;
    }

    public static boolean isometric() {
        return active && isometric;
    }

    // ---------------------------------------------------------------- lifecycle

    public static void toggle() {
        if (active) {
            exit();
        } else {
            enter();
        }
    }

    private static void enter() {
        LocalPlayer player = MC.player;
        if (player == null || MC.level == null) {
            return;
        }
        Options options = MC.options;
        camera = new PhotoCamera(MC.level, player.getEyePosition(), player.getYRot(), player.getXRot());

        savedCameraType = options.getCameraType();
        savedHideGui = options.hideGui;
        savedInput = player.input;
        savedSmartCull = MC.smartCull;

        options.setCameraType(CameraType.FIRST_PERSON);
        options.hideGui = true;
        player.input = new Input(); // the body stands still while the camera roams
        MC.setCameraEntity(camera);
        setCameraEyeHeight(0.0F);
        // The hand pass clears the depth buffer, which depth of field reads afterwards.
        MC.gameRenderer.setRenderHand(false);
        drainClicks();
        lastFrameNanos = System.nanoTime();
        active = true;

        if (isometric) {
            snapToIsometric();
        }
        applyCulling();
        applyDof();
        if (freezeWorld) {
            setServerFrozen(true);
        }

        if (!hintShown) {
            hintShown = true;
            PhotoHud.hint();
        } else {
            PhotoHud.message(Component.translatable("photomode.msg.on"));
        }
    }

    private static void exit() {
        LocalPlayer player = MC.player;
        active = false;
        MC.gameRenderer.shutdownEffect();
        MC.gameRenderer.setRenderHand(true);
        if (player != null) {
            player.input = savedInput;
            MC.setCameraEntity(player);
            setCameraEyeHeight(player.getEyeHeight());
        }
        MC.options.setCameraType(savedCameraType);
        MC.options.hideGui = savedHideGui;
        MC.smartCull = savedSmartCull;
        MC.levelRenderer.needsUpdate();
        setServerFrozen(false);
        drainClicks();
        pendingPhoto = false;
        camera = null;
        savedInput = null;
        PhotoHud.message(Component.translatable("photomode.msg.off"));
    }

    /** World closed under us: just forget the session, there's nothing left to restore. */
    static void reset() {
        if (active) {
            MC.gameRenderer.setRenderHand(true);
            MC.options.hideGui = savedHideGui;
            MC.options.setCameraType(savedCameraType);
            MC.smartCull = savedSmartCull;
        }
        active = false;
        camera = null;
        savedInput = null;
        serverFrozenByUs = false;
    }

    // ---------------------------------------------------------------- per tick / frame

    static void onTick() {
        while (PhotoKeys.TOGGLE.consumeClick()) {
            toggle();
        }
        if (!active) {
            for (KeyMapping key : PhotoKeys.ALL) {
                while (key.consumeClick()) {
                    // Swallow presses outside photo mode so they don't fire on entry.
                }
            }
            return;
        }
        if (MC.player == null || MC.level == null || camera == null || camera.level() != MC.level) {
            exit();
            return;
        }
        while (PhotoKeys.PLAYER.consumeClick()) {
            showPlayer = !showPlayer;
            PhotoHud.toggled("photomode.msg.player", showPlayer);
        }
        while (PhotoKeys.DOF.consumeClick()) {
            dof = !dof;
            applyDof();
            PhotoHud.toggled("photomode.msg.dof", dof);
        }
        while (PhotoKeys.ISOMETRIC.consumeClick()) {
            isometric = !isometric;
            if (isometric) {
                snapToIsometric();
            }
            applyCulling();
            PhotoHud.toggled("photomode.msg.isometric", isometric);
        }
        while (PhotoKeys.FREEZE.consumeClick()) {
            if (MC.getSingleplayerServer() == null) {
                PhotoHud.message(Component.translatable("photomode.msg.freeze_unavailable"));
            } else {
                freezeWorld = !freezeWorld;
                setServerFrozen(freezeWorld);
                PhotoHud.toggled("photomode.msg.freeze", freezeWorld);
            }
        }
        while (PhotoKeys.ROTATE_LEFT.consumeClick()) {
            rotate(-90.0F);
        }
        while (PhotoKeys.ROTATE_RIGHT.consumeClick()) {
            rotate(90.0F);
        }
        // Vanilla key handling is suspended in photo mode, so the attack button is free.
        while (MC.options.keyAttack.consumeClick()) {
            pendingPhoto = true;
        }
    }

    /** True for the one frame that gets saved, so photo mode's own messages stay out of it. */
    static boolean capturing() {
        return pendingPhoto;
    }

    /** After the frame is fully drawn and before it is shown: save it. */
    static void onFrameEnd() {
        if (!pendingPhoto) {
            return;
        }
        pendingPhoto = false;
        if (!active) {
            return;
        }
        Screenshot.grab(MC.gameDirectory, MC.getMainRenderTarget(),
                saved -> MC.execute(() -> PhotoHud.message(saved)));
    }

    static void onFrame() {
        long now = System.nanoTime();
        double dt = Math.min((now - lastFrameNanos) / 1.0e9, 0.1);
        lastFrameNanos = now;
        if (!active || camera == null) {
            return;
        }
        if (MC.screen == null && MC.getOverlay() == null) {
            fly(dt);
        }
        camera.settle();
        if (dof) {
            updateFocus(dt);
        }
    }

    // ---------------------------------------------------------------- input

    /** Mouse look. Returns true when consumed. */
    public static boolean turn(double dx, double dy) {
        if (!active || camera == null) {
            return false;
        }
        if (!isometric) {
            camera.turn(dx, dy);
            camera.settle();
        }
        return true;
    }

    /** Mouse wheel: fly speed; zoom in isometric; blur strength while sprint is held. */
    static boolean scroll(double delta) {
        if (!active || delta == 0.0) {
            return false;
        }
        if (dof && MC.options.keySprint.isDown()) {
            dofStrength = Mth.clamp(dofStrength * (float) Math.pow(1.2, delta), 0.1F, 10.0F);
            PhotoHud.message(Component.translatable("photomode.msg.dof_strength", String.format("%.2f", dofStrength)));
        } else if (isometric) {
            isoHalfHeight = Mth.clamp(isoHalfHeight / (float) Math.pow(1.15, delta), 1.0F, 512.0F);
            PhotoHud.message(Component.translatable("photomode.msg.zoom", String.format("%.0f", isoHalfHeight * 2)));
        } else {
            flySpeed = Mth.clamp(flySpeed * Math.pow(1.2, delta), 0.25, 256.0);
            PhotoHud.message(Component.translatable("photomode.msg.speed", String.format("%.1f", flySpeed)));
        }
        return true;
    }

    private static void fly(double dt) {
        Options o = MC.options;
        double forward = axis(o.keyUp, o.keyDown);
        double left = axis(o.keyLeft, o.keyRight);
        double up = axis(o.keyJump, o.keyShift);
        if (forward == 0 && left == 0 && up == 0) {
            return;
        }
        float yaw = camera.getYRot() * Mth.DEG_TO_RAD;
        Vec3 ahead = isometric
                ? new Vec3(-Mth.sin(yaw), 0, Mth.cos(yaw)) // pan across the ground
                : Vec3.directionFromRotation(camera.getXRot(), camera.getYRot());
        Vec3 side = new Vec3(Mth.cos(yaw), 0, Mth.sin(yaw));
        Vec3 dir = ahead.scale(forward).add(side.scale(left)).add(0, up, 0);
        double speed = isometric ? isoHalfHeight * 0.75 : flySpeed;
        camera.setPos(camera.position().add(dir.normalize().scale(speed * dt)));
    }

    private static double axis(KeyMapping positive, KeyMapping negative) {
        return (positive.isDown() ? 1 : 0) - (negative.isDown() ? 1 : 0);
    }

    private static void rotate(float degrees) {
        camera.setYRot(camera.getYRot() + degrees);
        camera.settle();
    }

    private static void snapToIsometric() {
        float yaw = Math.round((camera.getYRot() - 45.0F) / 90.0F) * 90.0F + 45.0F;
        camera.setYRot(yaw);
        camera.setXRot(ISO_PITCH);
        camera.settle();
    }

    // ---------------------------------------------------------------- rendering hooks

    /** Orthographic stand-in for the perspective projection while isometric is on. */
    public static Matrix4f isometricProjection(float depthFar) {
        float aspect = (float) MC.getWindow().getWidth() / Math.max(1, MC.getWindow().getHeight());
        float halfH = isoHalfHeight;
        // The slab reaches behind the camera too, so what you frame is centred on it.
        return new Matrix4f().setOrtho(-halfH * aspect, halfH * aspect, -halfH, halfH, isoNear(depthFar), depthFar);
    }

    private static float isoNear(float depthFar) {
        return -depthFar * 0.5F;
    }

    private static void applyCulling() {
        // Smart culling assumes a perspective eye; it drops visible sections in orthographic view.
        MC.smartCull = !isometric && savedSmartCull;
        MC.levelRenderer.needsUpdate();
    }

    private static void applyDof() {
        if (!active) {
            return;
        }
        PostChain current = MC.gameRenderer.currentEffect();
        boolean loaded = current != null && current.getName().equals(DOF_CHAIN.toString());
        if (dof && !loaded) {
            MC.gameRenderer.loadEffect(DOF_CHAIN);
            focus = measureFocus();
        } else if (!dof && loaded) {
            MC.gameRenderer.shutdownEffect();
        }
    }

    private static void updateFocus(double dt) {
        PostChain chain = MC.gameRenderer.currentEffect();
        if (chain == null || !chain.getName().equals(DOF_CHAIN.toString())) {
            return;
        }
        float target = measureFocus();
        focus += (target - focus) * (float) (1.0 - Math.exp(-dt * 6.0));
        float far = MC.gameRenderer.getDepthFar();
        chain.setUniform("Focus", focus);
        chain.setUniform("Strength", dofStrength);
        chain.setUniform("Ortho", isometric ? 1.0F : 0.0F);
        chain.setUniform("Near", isometric ? isoNear(far) : 0.05F);
        chain.setUniform("Far", far);
    }

    /** Autofocus: distance to whatever sits under the centre of the frame. */
    private static float measureFocus() {
        Vec3 eye = camera.position();
        Vec3 view = Vec3.directionFromRotation(camera.getXRot(), camera.getYRot());
        Vec3 end = eye.add(view.scale(FOCUS_RANGE));
        HitResult block = MC.level.clip(new ClipContext(eye, end, ClipContext.Block.VISUAL, ClipContext.Fluid.ANY,
                CollisionContext.empty()));
        double best = block.getType() == HitResult.Type.MISS ? FOCUS_RANGE : block.getLocation().distanceTo(eye);
        AABB box = new AABB(eye, eye).expandTowards(view.scale(best)).inflate(1.0);
        EntityHitResult entity = ProjectileUtil.getEntityHitResult(camera, eye, eye.add(view.scale(best)), box,
                e -> !e.isSpectator() && (e != MC.player || showPlayer), best * best);
        if (entity != null) {
            best = Math.min(best, entity.getLocation().distanceTo(eye));
        }
        // Distance along the view axis equals depth at the centre of the screen.
        return (float) Math.max(best, 0.1);
    }

    // ---------------------------------------------------------------- helpers

    private static void setCameraEyeHeight(float height) {
        CameraAccessor main = (CameraAccessor) MC.gameRenderer.getMainCamera();
        main.photomode$setEyeHeight(height);
        main.photomode$setEyeHeightOld(height);
    }

    /** Clicks queued while vanilla key handling was paused would otherwise fire on exit. */
    private static void drainClicks() {
        for (KeyMapping key : MC.options.keyMappings) {
            boolean ours = false;
            for (KeyMapping mine : PhotoKeys.ALL) {
                ours |= key == mine;
            }
            if (!ours) {
                while (key.consumeClick()) {
                    // discard
                }
            }
        }
    }

    private static void setServerFrozen(boolean frozen) {
        IntegratedServer server = MC.getSingleplayerServer();
        if (server == null) {
            return;
        }
        server.execute(() -> {
            ServerTickRateManager ticks = server.tickRateManager();
            if (frozen) {
                if (!ticks.isFrozen()) {
                    ticks.setFrozen(true);
                    serverFrozenByUs = true;
                }
            } else if (serverFrozenByUs) {
                ticks.setFrozen(false);
                serverFrozenByUs = false;
            }
        });
    }
}
