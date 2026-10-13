package io.github.knap43.arrowutilities.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.knap43.arrowutilities.StuckArrows;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {
    /** Vanilla slowly sheds stuck arrows; tracked ones stay until death. */
    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setArrowCount(I)V"))
    private void arrowutilities$keepArrows(LivingEntity self, int count, Operation<Void> original) {
        if (!StuckArrows.isPinned(self)) {
            original.call(self, count);
        }
    }
}
