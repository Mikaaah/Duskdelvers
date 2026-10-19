package com.townsandkingdoms.duskdelvers;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import java.util.List;

/**
 * Exploration-session anchor, kept under the original class name so early
 * 0.3 save data can still be loaded. NO rooms are ever generated.
 */
public final class ExpeditionLayout {
    private final int id;
    private final int tier;
    private final List<BlockPos> rooms;

    private ExpeditionLayout(int id,int tier,List<BlockPos> anchors){
        this.id=id;this.tier=tier;this.rooms=List.copyOf(anchors);
    }
    public int id(){return id;}
    public int tier(){return tier;}
    public List<BlockPos> rooms(){return rooms;}

    public static BlockPos base(int id,long seed){
        // New runs are 512 blocks apart. Region spacing controls initial spawn
        // only; players can freely explore across biome boundaries.
        int x=2048+(id%64)*512;
        int z=2048+(id/64)*512;
        return new BlockPos(x,72,z);
    }

    public static ExpeditionLayout at(int id,int tier,BlockPos arrival){
        return new ExpeditionLayout(id,tier,List.of(arrival));
    }

    public CompoundTag toTag(){
        CompoundTag data=new CompoundTag();
        data.putInt("id",id);
        data.putInt("tier",tier);
        ListTag list=new ListTag();
        for(BlockPos pos:rooms){
            CompoundTag block=new CompoundTag();
            block.putInt("x",pos.getX());
            block.putInt("y",pos.getY());
            block.putInt("z",pos.getZ());
            list.add(block);
        }
        data.put("rooms",list);
        data.putBoolean("completed",false);
        return data;
    }

    public static ExpeditionLayout fromTag(CompoundTag tag){
        ListTag list=tag.getList("rooms",Tag.TAG_COMPOUND);
        List<BlockPos> anchors=new java.util.ArrayList<>();
        for(int i=0;i<list.size();i++){
            var pos=list.getCompound(i);
            anchors.add(new BlockPos(pos.getInt("x"),pos.getInt("y"),pos.getInt("z")));
        }
        return new ExpeditionLayout(tag.getInt("id"),tag.getInt("tier"),anchors);
    }
}
