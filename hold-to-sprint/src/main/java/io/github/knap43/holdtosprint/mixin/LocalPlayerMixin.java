package io.github.knap43.holdtosprint.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
abstract class LocalPlayerMixin {
    @Unique
    private static boolean holdtosprint$keyDown() {
        // Respects the vanilla "Sprint: Toggle" option, where the key latches instead.
        return Minecraft.getInstance().options.keySprint.isDown();
    }

    /** Letting go of the sprint key ends the sprint, before this tick's movement is worked out. */
    @Inject(method = "aiStep", at = @At("HEAD"))
    private void holdtosprint$stopOnRelease(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        if (self.isSprinting() && !holdtosprint$keyDown()) {
            self.setSprinting(false);
        }
    }

    /**
     * The first sprint start in aiStep is vanilla's double-tap-forward path (it also fires when the
     * key is held, which the second path covers anyway). Only let it through with the key down.
     */
    @WrapOperation(
            method = "aiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setSprinting(Z)V", ordinal = 0))
    private void holdtosprint$noDoubleTap(LocalPlayer self, boolean sprinting, Operation<Void> original) {
        if (!sprinting || holdtosprint$keyDown()) {
            original.call(self, sprinting);
        }
    }
}
