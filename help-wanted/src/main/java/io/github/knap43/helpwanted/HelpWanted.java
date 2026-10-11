package io.github.knap43.helpwanted;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(HelpWanted.MOD_ID)
public final class HelpWanted {
    public static final String MOD_ID = "helpwanted";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MOD_ID);

    public static final DeferredBlock<HelpWantedBoardBlock> BOARD = BLOCKS.register("help_wanted_board",
            () -> new HelpWantedBoardBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(1.0F)
                    .sound(SoundType.WOOD)
                    .noOcclusion()
                    .ignitedByLava()));

    public static final DeferredItem<BlockItem> BOARD_ITEM = ITEMS.registerSimpleBlockItem(BOARD);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HelpWantedBoardBlockEntity>> BOARD_ENTITY =
            BLOCK_ENTITIES.register("help_wanted_board",
                    () -> BlockEntityType.Builder.of(HelpWantedBoardBlockEntity::new, BOARD.get()).build(null));

    public HelpWanted(IEventBus modBus, ModContainer container) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(HelpWanted::addToCreativeTab);
        container.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(BOARD_ITEM);
        }
    }
}
