package io.github.knap43.reversewheel.mixin;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MouseHandler.class)
abstract class MouseHandlerMixin {
    /** Hotbar slot selection. */
    @ModifyArg(
            method = "onScroll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;swapPaint(D)V"))
    private double reversewheel$hotbar(double direction) {
        return -direction;
    }

    /** The spectator hotbar (teleport menu), so it matches. */
    @ModifyArg(
            method = "onScroll",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;onMouseScrolled(I)V"))
    private int reversewheel$spectatorHotbar(int direction) {
        return -direction;
    }
}
