package io.github.knap43.reversewheel;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

/** All behaviour lives in {@link io.github.knap43.reversewheel.mixin.MouseHandlerMixin}. */
@Mod(value = ReverseWheel.MOD_ID, dist = Dist.CLIENT)
public final class ReverseWheel {
    public static final String MOD_ID = "reversewheel";
}
