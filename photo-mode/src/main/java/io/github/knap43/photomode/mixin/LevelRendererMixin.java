package io.github.knap43.photomode.mixin;

import io.github.knap43.photomode.PhotoController;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {
    /**
     * Vanilla turns off occlusion culling when a spectator's camera is inside a solid block,
     * otherwise nothing renders. The photo camera passes through blocks just the same.
     */
    @ModifyArg(
            method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;setupRender(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;ZZ)V"),
            index = 3)
    private boolean photomode$noclipCulling(boolean spectator) {
        return spectator || PhotoController.active();
    }
}
