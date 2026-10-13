package io.github.knap43.arrowutilities.mixin.client;

import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Lets stuck arrows follow the baby head/body scaling done in {@code AgeableListModel#renderToBuffer}. */
@Mixin(AgeableListModel.class)
public interface AgeableListModelAccessor {
    @Invoker("headParts")
    Iterable<ModelPart> arrowutilities$headParts();

    @Invoker("bodyParts")
    Iterable<ModelPart> arrowutilities$bodyParts();

    @Accessor("scaleHead")
    boolean arrowutilities$scaleHead();

    @Accessor("babyYHeadOffset")
    float arrowutilities$babyYHeadOffset();

    @Accessor("babyZHeadOffset")
    float arrowutilities$babyZHeadOffset();

    @Accessor("babyHeadScale")
    float arrowutilities$babyHeadScale();

    @Accessor("babyBodyScale")
    float arrowutilities$babyBodyScale();

    @Accessor("bodyYOffset")
    float arrowutilities$bodyYOffset();
}
