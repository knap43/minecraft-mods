package io.github.knap43.photomode.mixin;

import io.github.knap43.photomode.PhotoController;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
abstract class KeyboardHandlerMixin {
    /** Esc leaves photo mode instead of opening the pause menu. Losing focus still pauses as usual. */
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void photomode$escapeExits(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS && window == mc.getWindow().getWindow()
                && mc.screen == null && PhotoController.active()) {
            mc.execute(PhotoController::toggle);
            ci.cancel();
        }
    }
}
