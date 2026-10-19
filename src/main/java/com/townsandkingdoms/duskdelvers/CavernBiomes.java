package com.townsandkingdoms.duskdelvers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

import java.util.Map;

/**
 * Difficulty follows the ACTUAL worldgen biome, not the entrance portal tier.
 * All biomes are native Duskdelvers cave-adapted biomes; optional forest and sky
 * mods are not hard requirements.
 */
public final class CavernBiomes {
    private static final Map<String,Integer> TIERS=Map.ofEntries(
        Map.entry("limestone_hollows",1),
        Map.entry("mosslit_grotto",1),
        Map.entry("fungal_glades",1),
        Map.entry("twilight_thicket",2),
        Map.entry("dripstone_chasms",2),
        Map.entry("glimmering_depths",2),
        Map.entry("enchanted_grove",3),
        Map.entry("aether_glades",3),
        Map.entry("frostbound_caverns",3),
        Map.entry("glowing_highlands",3),
        Map.entry("resonant_crystalfields",4),
        Map.entry("golden_sanctuary",4),
        Map.entry("ashen_abyss",4),
        Map.entry("chaos_rift",5),
        Map.entry("shattered_void",5),
        Map.entry("soulbound_crypts",5)
    );

    private CavernBiomes(){}

    public static int tier(Holder<Biome> biome) {
        return biome.unwrapKey().map(key -> {
            ResourceLocation id=key.location();
            if(!id.getNamespace().equals(Duskdelvers.MOD_ID))return 1;
            return TIERS.getOrDefault(id.getPath(),1);
        }).orElse(1);
    }

    public static String name(Holder<Biome> biome) {
        return biome.unwrapKey().map(key -> {
            ResourceLocation id=key.location();
            String name=id.getPath().replace('_',' ');
            return Character.toUpperCase(name.charAt(0))+name.substring(1);
        }).orElse("Unknown caverns");
    }

    public static int count(){return TIERS.size();}
}
