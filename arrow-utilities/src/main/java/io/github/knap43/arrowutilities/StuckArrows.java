package io.github.knap43.arrowutilities;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/**
 * Server-side bookkeeping. Vanilla already syncs an arrow count for every living entity;
 * we keep the actual arrow items beside it, stop the count decaying, and drop the items on death.
 * Players keep vanilla behaviour.
 */
public final class StuckArrows {
    private StuckArrows() {}

    public static boolean tracks(LivingEntity entity) {
        return !(entity instanceof Player);
    }

    public static boolean isPinned(LivingEntity entity) {
        if (!tracks(entity)) {
            return false;
        }
        List<ItemStack> stuck = entity.getExistingDataOrNull(ArrowUtilities.STUCK_ARROWS);
        return stuck != null && !stuck.isEmpty();
    }

    /** Called from {@code AbstractArrow#onHitEntity} in place of the vanilla arrow-count bump. */
    public static void lodge(AbstractArrow arrow, LivingEntity target) {
        ItemStack stack = arrow.getPickupItemStackOrigin().copyWithCount(1);
        if (stack.isEmpty()) {
            return;
        }
        if (!target.isAlive()) {
            // The killing shot: death loot was already rolled inside hurt(), so drop it now.
            target.spawnAtLocation(stack);
            return;
        }
        List<ItemStack> stuck = new ArrayList<>(target.getData(ArrowUtilities.STUCK_ARROWS));
        stuck.add(stack);
        target.setData(ArrowUtilities.STUCK_ARROWS, List.copyOf(stuck));
        target.setArrowCount(stuck.size());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasData(ArrowUtilities.STUCK_ARROWS)) {
            return;
        }
        for (ItemStack stack : entity.removeData(ArrowUtilities.STUCK_ARROWS)) {
            ItemEntity item = new ItemEntity(entity.level(), entity.getX(), entity.getY(0.5), entity.getZ(), stack.copy());
            item.setDefaultPickUpDelay();
            event.getDrops().add(item);
        }
        entity.setArrowCount(0);
    }

    /** The arrow count is synced but not saved, so rebuild it from the saved items on load. */
    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity living && isPinned(living)) {
            living.setArrowCount(living.getData(ArrowUtilities.STUCK_ARROWS).size());
        }
    }

    /** Zombie → drowned, villager → witch, and so on: the arrows come along. */
    @SubscribeEvent
    public static void onConvert(LivingConversionEvent.Post event) {
        LivingEntity from = event.getEntity();
        LivingEntity to = event.getOutcome();
        if (!isPinned(from) || !tracks(to)) {
            return;
        }
        List<ItemStack> stuck = from.removeData(ArrowUtilities.STUCK_ARROWS);
        to.setData(ArrowUtilities.STUCK_ARROWS, stuck);
        to.setArrowCount(stuck.size());
    }
}
