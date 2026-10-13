package io.github.knap43.arrowutilities.client;

import io.github.knap43.arrowutilities.ArrowUtilities;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = ArrowUtilities.MOD_ID, dist = Dist.CLIENT)
public final class ArrowUtilitiesClient {
    public ArrowUtilitiesClient(IEventBus modBus) {
        modBus.addListener(ArrowUtilitiesClient::addLayers);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : event.getEntityTypes()) {
            if (type == EntityType.PLAYER) {
                continue; // players already have vanilla's layer
            }
            EntityRenderer<?> renderer = event.getRenderer((EntityType) type);
            if (renderer instanceof LivingEntityRenderer living) {
                living.addLayer(new MobArrowLayer(event.getContext(), living));
            }
        }
    }
}
