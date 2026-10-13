package io.github.knap43.photomode.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import io.github.knap43.photomode.PhotoController;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MouseHandler.class)
abstract class MouseHandlerMixin {
    /** Mouse look steers the photo camera instead of the player. */
    @WrapOperation(
            method = "turnPlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void photomode$turnCamera(LocalPlayer player, double dx, double dy, Operation<Void> original) {
        if (!PhotoController.turn(dx, dy)) {
            original.call(player, dx, dy);
        }
    }
}
