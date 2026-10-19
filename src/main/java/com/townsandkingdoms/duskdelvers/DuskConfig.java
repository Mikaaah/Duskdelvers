package com.townsandkingdoms.duskdelvers;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class DuskConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue MAX_ROOM_ENEMIES;
    public static final ModConfigSpec.BooleanValue EXPEDITIONS_ENABLED;
    public static final ModConfigSpec.BooleanValue MOB_SCALING;
    public static final ModConfigSpec.BooleanValue BOSS_REWARDS;

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
        SPEC = builder.build();
    }
    private DuskConfig() {}
}
