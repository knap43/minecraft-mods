package io.github.knap43.photomode.mixin;

import org.joml.Matrix4f;

import io.github.knap43.photomode.PhotoController;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @Shadow
    public abstract float getDepthFar();

    /** Used for both drawing and frustum culling, so the whole level goes orthographic. */
    @Inject(method = "getProjectionMatrix", at = @At("HEAD"), cancellable = true)
    private void photomode$isometric(double fov, CallbackInfoReturnable<Matrix4f> cir) {
        if (PhotoController.isometric()) {
            cir.setReturnValue(PhotoController.isometricProjection(this.getDepthFar()));
        }
    }
}
