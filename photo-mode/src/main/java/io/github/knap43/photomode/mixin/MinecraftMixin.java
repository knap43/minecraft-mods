package io.github.knap43.photomode.mixin;

import io.github.knap43.photomode.PhotoController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    /** No attacking, using, hotbar, inventory, perspective or chat keys while composing a shot. */
    @Inject(method = "handleKeybinds", at = @At("HEAD"), cancellable = true)
    private void photomode$suspendKeybinds(CallbackInfo ci) {
        if (PhotoController.active()) {
            ci.cancel();
        }
    }
}
