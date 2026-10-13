package io.github.knap43.arrowutilities.client;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Vanilla's player arrow layer, taught to work on any model.
 *
 * <p>It extends {@link ArrowLayer} on purpose: the actual arrow drawing is left to
 * {@code ArrowLayer#renderStuckItem}, which is exactly what 2D Projectiles hooks to draw its
 * flat arrows. Only the placement ({@code render}) is replaced, since vanilla's version asks a
 * {@link PlayerModel} for a random part. The {@code M} bound is never used here, so no cast to
 * {@code PlayerModel} is ever emitted for other models.
 */
public class MobArrowLayer<T extends LivingEntity, M extends PlayerModel<T>> extends ArrowLayer<T, M> {
    private final LivingEntityRenderer<?, ?> renderer;

    public MobArrowLayer(EntityRendererProvider.Context context, LivingEntityRenderer<T, M> renderer) {
        super(context, renderer);
        this.renderer = renderer;
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity, float limbSwing,
            float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        int count = this.numStuck(entity);
        if (count <= 0) {
            return;
        }
        EntityModel<?> model = this.renderer.getModel();
        List<ModelAnchors.Anchor> anchors = ModelAnchors.visible(model);
        if (anchors.isEmpty()) {
            return;
        }
        RandomSource random = RandomSource.create(entity.getId());
        for (int i = 0; i < count; i++) {
            pose.pushPose();
            ModelAnchors.Anchor anchor = anchors.get(random.nextInt(anchors.size()));
            anchor.apply(pose, model);
            ModelPart.Cube cube = anchor.part().getRandomCube(random);
            float fx = random.nextFloat();
            float fy = random.nextFloat();
            float fz = random.nextFloat();
            pose.translate(
                    Mth.lerp(fx, cube.minX, cube.maxX) / 16.0F,
                    Mth.lerp(fy, cube.minY, cube.maxY) / 16.0F,
                    Mth.lerp(fz, cube.minZ, cube.maxZ) / 16.0F);
            this.renderStuckItem(pose, buffers, light, entity,
                    -(fx * 2.0F - 1.0F), -(fy * 2.0F - 1.0F), -(fz * 2.0F - 1.0F), partialTick);
            pose.popPose();
        }
    }
}
