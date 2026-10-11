package io.github.knap43.helpwanted;

import java.util.Locale;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.event.EventHooks;

public class HelpWantedBoardBlockEntity extends BlockEntity {
    /** How often (in ticks) the board wakes up to see whether an applicant is due. */
    private static final int CHECK_INTERVAL = 100;
    private static final String TAG_NEXT_ARRIVAL = "NextArrival";

    /** Game time at which the next applicant may arrive; -1 until first scheduled. */
    private long nextArrival = -1L;

    public HelpWantedBoardBlockEntity(BlockPos pos, BlockState state) {
        super(HelpWanted.BOARD_ENTITY.get(), pos, state);
    }

    void serverTick(ServerLevel level, BlockPos pos) {
        long now = level.getGameTime();
        if (nextArrival < 0) {
            schedule(now);
            return;
        }
        // Spread boards across ticks so many of them never wake up together.
        if (Math.floorMod(now + pos.asLong(), CHECK_INTERVAL) != 0) {
            return;
        }
        // Honour a shortened interval in the config without waiting out the old one.
        long interval = Config.intervalTicks();
        if (nextArrival - now > interval) {
            nextArrival = now + interval;
            setChanged();
        }
        if (now < nextArrival || findFreeBed(level, pos).isEmpty()) {
            return;
        }
        if (spawnApplicant(level, pos)) {
            schedule(now);
        }
    }

    void describeStatus(ServerLevel level, ServerPlayer player) {
        Component message;
        if (findFreeBed(level, worldPosition).isEmpty()) {
            message = Component.translatable("message.helpwanted.no_bed");
        } else {
            long remaining = nextArrival < 0 ? Config.intervalTicks() : nextArrival - level.getGameTime();
            message = remaining <= 0
                    ? Component.translatable("message.helpwanted.soon")
                    : Component.translatable("message.helpwanted.next",
                            String.format(Locale.ROOT, "%.1f", remaining / 24000.0));
        }
        player.displayClientMessage(message, true);
    }

    private void schedule(long now) {
        nextArrival = now + Config.intervalTicks();
        setChanged();
    }

    private static Optional<BlockPos> findFreeBed(ServerLevel level, BlockPos pos) {
        return level.getPoiManager().findClosest(
                type -> type.is(PoiTypes.HOME),
                pos,
                Config.BED_SEARCH_RADIUS.get(),
                PoiManager.Occupancy.HAS_SPACE);
    }

    private boolean spawnApplicant(ServerLevel level, BlockPos pos) {
        Optional<BlockPos> spot = findSpawnSpot(level, pos);
        if (spot.isEmpty()) {
            return false;
        }
        Villager villager = EntityType.VILLAGER.create(level);
        if (villager == null) {
            return false;
        }
        BlockPos at = spot.get();
        villager.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
        EventHooks.finalizeMobSpawn(villager, level, level.getCurrentDifficultyAt(at), MobSpawnType.EVENT, null);
        villager.setVillagerData(villager.getVillagerData()
                .setType(VillagerType.byBiome(level.getBiome(at)))
                .setProfession(VillagerProfession.NONE));
        level.addFreshEntityWithPassengers(villager);

        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                villager.getX(), villager.getY() + 1.0, villager.getZ(), 12, 0.4, 0.6, 0.4, 0.0);
        level.playSound(null, at, SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
        return true;
    }

    /** A standable, unobstructed spot a few blocks from the board, close to its height. */
    private static Optional<BlockPos> findSpawnSpot(ServerLevel level, BlockPos board) {
        RandomSource random = level.random;
        for (int attempt = 0; attempt < 32; attempt++) {
            int dx = random.nextInt(13) - 6;
            int dz = random.nextInt(13) - 6;
            if (dx * dx + dz * dz < 9) {
                continue;
            }
            for (int dy : new int[] {0, 1, -1, 2, -2, 3, -3, 4, -4}) {
                BlockPos candidate = board.offset(dx, dy, dz);
                if (canStandAt(level, candidate)) {
                    return Optional.of(candidate);
                }
            }
        }
        Direction front = level.getBlockState(board).getValue(HorizontalDirectionalBlock.FACING);
        BlockPos fallback = board.relative(front);
        return canStandAt(level, fallback) ? Optional.of(fallback) : Optional.empty();
    }

    private static boolean canStandAt(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return false;
        }
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                && level.getFluidState(pos).isEmpty()
                && level.getFluidState(pos.above()).isEmpty()
                && level.noCollision(EntityType.VILLAGER.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong(TAG_NEXT_ARRIVAL, nextArrival);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        nextArrival = tag.contains(TAG_NEXT_ARRIVAL) ? tag.getLong(TAG_NEXT_ARRIVAL) : -1L;
    }
}
