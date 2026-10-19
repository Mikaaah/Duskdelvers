package com.townsandkingdoms.duskdelvers;

import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.List;

public final class DuskConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue MAX_OPTIONAL_MOBS;
    public static final ModConfigSpec.BooleanValue EXPEDITIONS_ENABLED;
    public static final ModConfigSpec.BooleanValue MOB_SCALING;
    public static final ModConfigSpec.BooleanValue TIMERS_ENABLED;
    public static final ModConfigSpec.BooleanValue DISPLAY_TIMER;
    public static final ModConfigSpec.IntValue OUTPOST_MINUTES;
    public static final ModConfigSpec.IntValue EXTRACTION_GRACE_MINUTES;
    public static final List<ModConfigSpec.IntValue> TIER_MINUTES;

    public static final ModConfigSpec.IntValue DAILY_RUNS;
    public static final ModConfigSpec.IntValue MAX_PARTY_SIZE;
    public static final ModConfigSpec.IntValue PARTY_RADIUS;
    public static final ModConfigSpec.BooleanValue KEEP_INVENTORY;
    public static final ModConfigSpec.BooleanValue KEEP_EXPERIENCE;
    public static final ModConfigSpec.BooleanValue FTB_TEAMS;
    public static final ModConfigSpec.BooleanValue TNK_LOOT;
    public static final ModConfigSpec.BooleanValue OPTIONAL_MOBS;
    public static final ModConfigSpec.IntValue UNIDENTIFIED_REWARDS;

    public static final List<ModConfigSpec.ConfigValue<String>> FTB_QUEST_IDS;
    public static final ModConfigSpec.ConfigValue<String> SKILLS_CATEGORY;
    public static final ModConfigSpec.IntValue SKILLS_XP_PER_TIER;
    private static final java.util.Map<String,ModConfigSpec.BooleanValue> PROVIDERS = new java.util.LinkedHashMap<>();

    public static boolean modAllowed(String mod){
        ModConfigSpec.BooleanValue setting=PROVIDERS.get(mod);
        return net.neoforged.fml.ModList.get().isLoaded(mod)
                && (setting==null||setting.get());
    }

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.comment("Duskdelvers settings for this server").push("expeditions");
        EXPEDITIONS_ENABLED = builder.comment("Enable generated expeditions.")
                .define("enabled", true);
        MAX_OPTIONAL_MOBS = builder.comment("Maximum ambient optional-mod creatures near one player in the caves.")
                .defineInRange("maxOptionalMobs", 6, 0, 24);
        MOB_SCALING = builder.comment("Scale mob attributes with expedition tier.")
                .define("scaleMobAttributes", true);
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
                "Minimum time remaining after first natural cache discovery, giving players time to explore.")
                .defineInRange("extractionGraceMinutes", 3, 0, 30);
        builder.pop();
        builder.comment("Limited attempts are counted per player using UTC calendar days. Zero disables the limit.")
                .push("entryLimits");
        DAILY_RUNS = builder.comment("Maximum new expedition entries per player per UTC day. All party members spend one entry.")
                .defineInRange("entriesPerDay", 3, 0, 100);
        MAX_PARTY_SIZE = builder.comment("Maximum players in a shared run, including the leader.")
                .defineInRange("maxPartySize", 4, 1, 16);
        PARTY_RADIUS = builder.comment("Party members must be this close to the leader to enter together.")
                .defineInRange("gatherRadius", 16, 4, 64);
        FTB_TEAMS = builder.comment("Enable opt-in grouping through FTB Teams when installed.")
                .define("ftbTeams", true);
        builder.pop();
        builder.comment("Protection on deaths occurring in the Chaos Caverns; never changes overworld gamerules.")
                .push("deathProtection");
        KEEP_INVENTORY = builder.comment("Restore the vanilla inventory after death in the Chaos Caverns.")
                .define("keepInventory", true);
        KEEP_EXPERIENCE = builder.comment("Keep all vanilla experience on Chaos Caverns death.")
                .define("keepExperience", true);
        builder.pop();
        builder.comment("All integrations are optional, disabled automatically if their mods are missing.")
                .push("compatibility");
        TNK_LOOT = builder.comment("Create TNK Loot unidentified items on dungeon boss completion.")
                .define("tnkLoot", true);
        OPTIONAL_MOBS = builder.comment("Enable safe registry-validated modded mob pools.")
                .define("optionalCreaturePools", true);
        UNIDENTIFIED_REWARDS = builder.comment("TNK Loot unidentified tokens per eligible participant after boss victory.")
                .defineInRange("tokensPerPlayer", 1, 0, 5);
        builder.pop();
        builder.comment("Optional content providers. All are runtime-detected and can be disabled individually.")
                .push("providers");
        for(String provider : List.of("tnkloot","apotheosis","epicfight","irons_spellbooks",
                "alexscaves","cataclysm","twilightforest","aether","create",
                "ars_nouveau","ftbteams","ftbquests","puffish_skills")){
            PROVIDERS.put(provider,builder.define(provider,true));
        }
        builder.pop();
        builder.comment("Integrations requiring modpack-defined quest/category IDs.")
                .push("progression");
        java.util.ArrayList<ModConfigSpec.ConfigValue<String>> quests=new java.util.ArrayList<>();
        for(int tier=1;tier<=5;tier++){
            quests.add(builder.comment("FTB Quests 64-bit hex quest ID for clearing Tier "+tier+". Blank skips.")
                    .define("tier"+tier+"QuestId",""));
        }
        FTB_QUEST_IDS=List.copyOf(quests);
        SKILLS_CATEGORY=builder.comment("Puffish Skills XP category, e.g. tnk3:exploration. Empty skips.")
                .define("skillsCategory","");
        SKILLS_XP_PER_TIER=builder.comment("XP granted to configured Puffish category per tier.")
                .defineInRange("skillsXpPerTier",50,0,100000);
        builder.pop();
        SPEC = builder.build();
    }
    private DuskConfig() {}
}
