package com.townsandkingdoms.duskdelvers;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Small scattered natural floor patches with optional Twilight Forest / Aether
 * blocks when installed. Never creates rooms, artificial walls or structures.
 */
public final class CavernAccentFeature extends Feature<NoneFeatureConfiguration> {
    public CavernAccentFeature(Codec<NoneFeatureConfiguration> codec) { super(codec); }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level=context.level();
        RandomSource rng=context.random();
        BlockPos origin=context.origin();
        String biome=level.getBiome(origin).unwrapKey().map(key ->
                key.location().getNamespace().equals("duskdelvers")
                        ?key.location().getPath():"").orElse("");
        if(biome.isBlank())return false;
        Block accent=accent(biome,rng);
        for(int delta=0;delta<28;delta++){
            BlockPos pos=origin.below(delta);
            if(pos.getY()<13)break;
            if(!level.getBlockState(pos).isAir()||
                    !level.getBlockState(pos.above()).isAir())continue;
            BlockPos floor=pos.below();
            if(!level.getBlockState(floor).isSolidRender(level,floor))continue;
            if(level.getBlockState(floor).is(Blocks.BEDROCK))continue;
            if(!level.getFluidState(floor).isEmpty())continue;
            int changed=0;
            for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++){
                if(rng.nextFloat()>.55F)continue;
                BlockPos target=floor.offset(x,0,z);
                if((target.getX()>>4)!=(origin.getX()>>4)||
                        (target.getZ()>>4)!=(origin.getZ()>>4))continue;
                if(!level.getBlockState(target).isSolidRender(level,target)||
                        !level.getBlockState(target.above()).isAir())continue;
                if(level.getBlockState(target).is(Blocks.BEDROCK))continue;
                if(level.setBlock(target,accent.defaultBlockState(),2))changed++;
            }
            return changed>0;
        }
        return false;
    }

    private static Block accent(String biome,RandomSource rng){
        return switch(biome){
            case "twilight_thicket","enchanted_grove" ->
                    ExpeditionCompat.findBlock("twilightforest","twilightforest:twilight_oak_log",
                            rng.nextBoolean()?Blocks.MOSSY_COBBLESTONE:Blocks.ROOTED_DIRT);
            case "aether_glades","golden_sanctuary","glowing_highlands" ->
                    ExpeditionCompat.findBlock("aether","aether:holystone",
                            rng.nextBoolean()?Blocks.CALCITE:Blocks.SMOOTH_SANDSTONE);
            case "fungal_glades" -> rng.nextBoolean()?Blocks.MYCELIUM:Blocks.MUSHROOM_STEM;
            case "frostbound_caverns" -> rng.nextBoolean()?Blocks.PACKED_ICE:Blocks.SNOW_BLOCK;
            case "resonant_crystalfields" -> rng.nextInt(6)==0?Blocks.AMETHYST_BLOCK:Blocks.CALCITE;
            case "ashen_abyss" -> rng.nextBoolean()?Blocks.BLACKSTONE:Blocks.BASALT;
            case "chaos_rift","shattered_void","soulbound_crypts" ->
                    rng.nextInt(9)==0?Blocks.CRYING_OBSIDIAN:Blocks.POLISHED_BLACKSTONE;
            case "mosslit_grotto","glimmering_depths" ->
                    rng.nextBoolean()?Blocks.MOSS_BLOCK:Blocks.CLAY;
            case "dripstone_chasms","limestone_hollows" ->
                    rng.nextBoolean()?Blocks.DRIPSTONE_BLOCK:Blocks.CALCITE;
            default -> Blocks.TUFF;
        };
    }
}
