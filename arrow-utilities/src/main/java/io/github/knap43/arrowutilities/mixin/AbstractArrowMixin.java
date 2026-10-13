package io.github.knap43.arrowutilities.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.knap43.arrowutilities.StuckArrows;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractArrow.class)
abstract class AbstractArrowMixin {
    /** Vanilla bumps the target's arrow count here (server side, non-piercing hits only). */
    @WrapOperation(
            method = "onHitEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setArrowCount(I)V"))
    private void arrowutilities$lodge(LivingEntity target, int count, Operation<Void> original) {
        if (StuckArrows.tracks(target)) {
            StuckArrows.lodge((AbstractArrow) (Object) this, target);
        } else {
            original.call(target, count);
        }
    }
}
