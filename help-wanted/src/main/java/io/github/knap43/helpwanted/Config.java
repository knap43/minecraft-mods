package io.github.knap43.helpwanted;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Per-world settings, stored in {@code <world>/serverconfig/helpwanted-server.toml}. */
public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue ARRIVAL_INTERVAL_DAYS = BUILDER
            .comment("In-game days between applicants. One day = 24000 ticks = 20 real minutes.")
            .defineInRange("arrivalIntervalDays", 3.0, 0.05, 100.0);

    public static final ModConfigSpec.IntValue BED_SEARCH_RADIUS = BUILDER
            .comment("How far (in blocks) from the board to look for an unclaimed bed.")
            .defineInRange("bedSearchRadius", 48, 8, 128);

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static long intervalTicks() {
        return Math.max(1L, Math.round(ARRIVAL_INTERVAL_DAYS.get() * 24000.0));
    }

    private Config() {}
}
