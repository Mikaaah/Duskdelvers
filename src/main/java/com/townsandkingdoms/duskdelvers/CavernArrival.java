package com.townsandkingdoms.duskdelvers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

/**
 * Choose safe naturally existing cave space. Generate no dungeon room, corridor
 * or boss arena. Searches at most six climate samples per expedition.
 */
public final class CavernArrival {
    private CavernArrival(){}

    public static BlockPos locate(ServerLevel level,int runId,int desiredTier){
        BlockPos base=ExpeditionLayout.base(runId,level.getSeed());
        BlockPos fallback=null;
        int bestDistance=Integer.MAX_VALUE;
        // All samples stay in the run's reserved starting region; worldgen for
        // at most six chunks, not an unbounded biome locate command.
        for(int attempt=0;attempt<6;attempt++){
            int dx=(attempt%3)*96 + 16;
            int dz=(attempt/3)*96 + 16;
            int x=base.getX()+dx;
            int z=base.getZ()+dz;
            level.getChunk(x>>4,z>>4);
            int tier=CavernBiomes.tier(level.getBiome(new BlockPos(x,64,z)));
            int difference=Math.abs(desiredTier-tier);
            if(difference>bestDistance)continue;
            BlockPos point=findFloor(level,x,z);
            if(point==null)continue;
            if(difference<bestDistance){bestDistance=difference;fallback=point;}
            if(difference==0)return point;
        }
        if(fallback!=null)return fallback;

        // Emergency 3x3 landing pocket. Only used if the sampled terrain has no
        // safe air column; this is not a generated structure or treasure room.
        int x=base.getX()+16,z=base.getZ()+16;
        int y=72;
        level.getChunk(x>>4,z>>4);
        BlockPos pos=new BlockPos(x,y,z);
        for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){
            level.setBlock(pos.offset(dx,-1,dz),Blocks.STONE.defaultBlockState(),2);
            for(int dy=0;dy<=3;dy++)
                level.setBlock(pos.offset(dx,dy,dz),Blocks.AIR.defaultBlockState(),2);
        }
        return pos;
    }

    private static BlockPos findFloor(ServerLevel level,int x,int z){
        for(int y=100;y>=25;y--){
            BlockPos pos=new BlockPos(x,y,z);
            if(isSafe(level,pos))return pos;
        }
        return null;
    }

    private static boolean isSafe(ServerLevel level,BlockPos pos){
        if(!level.getBlockState(pos).isAir()||
                !level.getBlockState(pos.above()).isAir()||
                !level.getBlockState(pos.above(2)).isAir()||
                !level.getFluidState(pos).isEmpty())return false;
        BlockPos below=pos.below();
        if(level.getBlockState(below).isAir()||
                level.getBlockState(below).is(Blocks.BEDROCK)||
                !level.getFluidState(below).isEmpty())return false;
        return level.getBlockState(below).isSolidRender(level,below);
    }
}
