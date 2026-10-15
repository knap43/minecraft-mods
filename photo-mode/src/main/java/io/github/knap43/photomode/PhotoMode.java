package io.github.knap43.photomode;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = PhotoMode.MOD_ID, dist = Dist.CLIENT)
public final class PhotoMode {
    public static final String MOD_ID = "photomode";

    public PhotoMode(IEventBus modBus) {
        modBus.addListener(PhotoMode::registerKeys);
        modBus.addListener(PhotoMode::registerHud);

        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre e) -> PhotoController.onTick());
        NeoForge.EVENT_BUS.addListener((RenderFrameEvent.Pre e) -> PhotoController.onFrame());
        NeoForge.EVENT_BUS.addListener((RenderFrameEvent.Post e) -> PhotoController.onFrameEnd());
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> PhotoController.reset());
        NeoForge.EVENT_BUS.addListener((InputEvent.MouseScrollingEvent e) -> {
            if (PhotoController.scroll(e.getScrollDeltaY())) {
                e.setCanceled(true);
            }
        });
    }

    private static void registerKeys(RegisterKeyMappingsEvent event) {
        for (var key : PhotoKeys.ALL) {
            event.register(key);
        }
    }

    private static void registerHud(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(MOD_ID, "messages"), PhotoHud::render);
    }
}
