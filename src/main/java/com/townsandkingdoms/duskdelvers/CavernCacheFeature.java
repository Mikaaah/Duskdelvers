package com.townsandkingdoms.duskdelvers;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * A rare, natural cave-floor find. No jigsaw parts, artificial corridors,
 * dungeon rooms, or chunk tickets. Loot table is chosen by cave biome tier.
 */
public final class CavernCacheFeature extends Feature<NoneFeatureConfiguration> {
    public CavernCacheFeature(Codec<NoneFeatureConfiguration> codec){super(codec);}

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        WorldGenLevel level=ctx.level();
        BlockPos origin=ctx.origin();
        RandomSource random=ctx.random();

        // The candidate is placed within the currently generating chunk;
        // check a short vertical column for a real, naturally accessible cave floor.
        // Avoid blind placement inside rock and avoid replacing existing world features.
        for(int offset=0;offset<40;offset++) {
            int delta=offset==0?0:(offset%2==0 ? -((offset+1)/2) : ((offset+1)/2));
            BlockPos pos=origin.offset(0,delta,0);
            if(pos.getY()<12||pos.getY()>119)continue;
            if(!level.getBlockState(pos).isAir())continue;
            if(!level.getBlockState(pos.above()).isAir())continue;
            var below=level.getBlockState(pos.below());
            if(below.isAir()||below.is(Blocks.BEDROCK)||below.getFluidState().isSource())continue;
            if(!below.isSolidRender(level,pos.below()))continue;
            if(!level.getFluidState(pos).isEmpty()||!level.getFluidState(pos.above()).isEmpty())continue;

            int tier=CavernBiomes.tier(level.getBiome(pos));
            // An isolated barrel is intentionally the whole "feature";
            // no more boxed structures are generated.
            if(!level.setBlock(pos,Blocks.BARREL.defaultBlockState(),2))return false;
            ResourceKey<LootTable> loot=ResourceKey.create(
                    Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath(Duskdelvers.MOD_ID,"chests/cavern_cache_tier_"+tier));
            RandomizableContainer.setBlockEntityLootTable(level,random,pos,loot);
            return true;
        }
        return false;
    }
}
