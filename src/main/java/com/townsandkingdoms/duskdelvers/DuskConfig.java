package com.townsandkingdoms.duskdelvers;

import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.List;

public final class DuskConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue MAX_ROOM_ENEMIES;
    public static final ModConfigSpec.BooleanValue EXPEDITIONS_ENABLED;
    public static final ModConfigSpec.BooleanValue MOB_SCALING;
    public static final ModConfigSpec.BooleanValue BOSS_REWARDS;
    public static final ModConfigSpec.BooleanValue TIMERS_ENABLED;
    public static final ModConfigSpec.BooleanValue DISPLAY_TIMER;
    public static final ModConfigSpec.IntValue OUTPOST_MINUTES;
    public static final ModConfigSpec.IntValue EXTRACTION_GRACE_MINUTES;
    public static final List<ModConfigSpec.IntValue> TIER_MINUTES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Duskdelvers settings for this server").push("expeditions");
        EXPEDITIONS_ENABLED = builder.comment("Enable generated expeditions.")
                .define("enabled", true);
        MAX_ROOM_ENEMIES = builder.comment("Hard cap on spawned enemies per room.")
                .defineInRange("maxRoomEnemies", 12, 1, 30);
        MOB_SCALING = builder.comment("Scale mob attributes with expedition tier.")
                .define("scaleMobAttributes", true);
        BOSS_REWARDS = builder.comment("Generate a reward chest after a boss is defeated.")
                .define("bossRewards", true);
        builder.pop();
        builder.comment("Per-player time limits inside the Chaos Caverns. Time only runs while online and inside the dimension.")
                .push("timeLimits");
        TIMERS_ENABLED = builder.comment("Disable to allow unlimited visits to the Chaos Caverns.")
                .define("enabled", true);
        DISPLAY_TIMER = builder.comment("Show a countdown in the action bar once per second.")
                .define("actionBarCountdown", true);
        OUTPOST_MINUTES = builder.comment("Maximum time to stay in the non-expedition arrival area.")
                .defineInRange("outpostMinutes", 30, 1, 240);
        ModConfigSpec.IntValue tier1 = builder.comment("Tier I: Forgotten Depths (minutes).")
                .defineInRange("tier1Minutes", 20, 1, 240);
        ModConfigSpec.IntValue tier2 = builder.comment("Tier II: Shattered Hollows (minutes).")
                .defineInRange("tier2Minutes", 25, 1, 240);
        ModConfigSpec.IntValue tier3 = builder.comment("Tier III: Resonant Abyss (minutes).")
                .defineInRange("tier3Minutes", 30, 1, 240);
        ModConfigSpec.IntValue tier4 = builder.comment("Tier IV: Ancient Dominion (minutes).")
                .defineInRange("tier4Minutes", 35, 1, 240);
        ModConfigSpec.IntValue tier5 = builder.comment("Tier V: Chaos Unbound (minutes).")
                .defineInRange("tier5Minutes", 40, 1, 240);
        TIER_MINUTES = List.of(tier1, tier2, tier3, tier4, tier5);
        EXTRACTION_GRACE_MINUTES = builder.comment(
                "Minimum time remaining after defeating the final guardian, giving players time to claim loot.")
                .defineInRange("extractionGraceMinutes", 3, 0, 30);
        builder.pop();
        SPEC = builder.build();
    }
    private DuskConfig() {}
}
