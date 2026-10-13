package io.github.knap43.photomode.mixin;

import io.github.knap43.photomode.PhotoController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
abstract class EntityRenderDispatcherMixin {
    /** Hides the player's body, shadow and name tag together. */
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void photomode$hidePlayer(E entity, Frustum frustum, double x, double y, double z,
            CallbackInfoReturnable<Boolean> cir) {
        if (PhotoController.hidesPlayer() && entity == Minecraft.getInstance().player) {
            cir.setReturnValue(false);
        }
    }
}
