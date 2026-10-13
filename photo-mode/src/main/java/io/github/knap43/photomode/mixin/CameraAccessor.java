package io.github.knap43.photomode.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The camera eases its eye height between entities; photo mode snaps it instead. */
@Mixin(Camera.class)
public interface CameraAccessor {
    @Accessor("eyeHeight")
    void photomode$setEyeHeight(float height);

    @Accessor("eyeHeightOld")
    void photomode$setEyeHeightOld(float height);
}
