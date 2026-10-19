package com.townsandkingdoms.duskdelvers;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** Connected, nonoverlapping dungeon rooms in a reserved 192x192 world region. */
public final class ExpeditionLayout {
    public static final int FLOOR_Y = 69;
    public static final int SPACING = 23;
    private final int id;
    private final int tier;
    private final List<BlockPos> rooms;

    private ExpeditionLayout(int id, int tier, List<BlockPos> rooms) {
        this.id = id;
        this.tier = tier;
        this.rooms = List.copyOf(rooms);
    }
    public int id() { return id; }
    public int tier() { return tier; }
    public List<BlockPos> rooms() { return rooms; }

    public static ExpeditionLayout generate(int id, int tier, long seed) {
        // An expedition occupies one 192x192 cell; rooms stay within its first 100 blocks.
        int baseX = 1024 + (id % 48) * 192;
        int baseZ = 1024 + (id / 48) * 192;
        int target = 4 + tier;
        List<int[]> best = new ArrayList<>();
        Random rng = new Random(seed ^ (0x9E3779B97F4A7C15L * id));

        for (int attempt = 0; attempt < 80 && best.size() < target; attempt++) {
            List<int[]> route = new ArrayList<>();
            Set<Integer> visited = new HashSet<>();
            int gx = 0, gz = 0;
            route.add(new int[] {gx, gz});
            visited.add(0);
            while (route.size() < target) {
                List<int[]> choices = new ArrayList<>();
                if (gx > 0 && !visited.contains(gx-1+gz*4)) choices.add(new int[]{gx-1,gz});
                if (gx < 3 && !visited.contains(gx+1+gz*4)) choices.add(new int[]{gx+1,gz});
                if (gz > 0 && !visited.contains(gx+(gz-1)*4)) choices.add(new int[]{gx,gz-1});
                if (gz < 3 && !visited.contains(gx+(gz+1)*4)) choices.add(new int[]{gx,gz+1});
                if (choices.isEmpty()) break;
                int[] next = choices.get(rng.nextInt(choices.size()));
                gx=next[0]; gz=next[1];
                route.add(next);
                visited.add(gx+gz*4);
            }
            if (route.size() > best.size()) best=route;
        }
        List<BlockPos> blocks = new ArrayList<>();
        for (int[] grid : best) {
            blocks.add(new BlockPos(baseX + grid[0]*SPACING, FLOOR_Y, baseZ + grid[1]*SPACING));
        }
        return new ExpeditionLayout(id, tier, blocks);
    }

    public CompoundTag toTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("id", id);
        tag.putInt("tier", tier);
        ListTag list = new ListTag();
        for (BlockPos pos : rooms) {
            CompoundTag room = new CompoundTag();
            room.putInt("x", pos.getX());
            room.putInt("y", pos.getY());
            room.putInt("z", pos.getZ());
            list.add(room);
        }
        tag.put("rooms",list);
        tag.putInt("activated",0);
        tag.putInt("cleared",0);
        tag.putBoolean("completed",false);
        return tag;
    }

    public static ExpeditionLayout fromTag(CompoundTag tag) {
        List<BlockPos> rooms = new ArrayList<>();
        ListTag list = tag.getList("rooms", Tag.TAG_COMPOUND);
        for (int i=0;i<list.size();i++) {
            CompoundTag p=list.getCompound(i);
            rooms.add(new BlockPos(p.getInt("x"),p.getInt("y"),p.getInt("z")));
        }
        return new ExpeditionLayout(tag.getInt("id"),tag.getInt("tier"),rooms);
    }
}
