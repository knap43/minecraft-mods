package io.github.knap43.arrowutilities;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(ArrowUtilities.MOD_ID)
public final class ArrowUtilities {
    public static final String MOD_ID = "arrowutilities";

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);

    /** The arrow items lodged in a (non-player) living entity, one stack of size 1 per arrow. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<List<ItemStack>>> STUCK_ARROWS =
            ATTACHMENTS.register("stuck_arrows", () -> AttachmentType.<List<ItemStack>>builder(() -> List.of())
                    .serialize(new StuckArrowsSerializer())
                    .build());

    public ArrowUtilities(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        NeoForge.EVENT_BUS.register(StuckArrows.class);
    }
}
