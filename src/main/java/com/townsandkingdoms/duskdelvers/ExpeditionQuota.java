package com.townsandkingdoms.duskdelvers;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Persistent quota: all players pay once per NEW expedition, with UTC-day reset. */
public final class ExpeditionQuota extends SavedData {
    private static final String KEY = "duskdelvers_daily_entries";
    private record Entry(long utcDay, int used) {}
    private final Map<UUID,Entry> entries=new HashMap<>();

    private static ExpeditionQuota load(CompoundTag tag,HolderLookup.Provider registry) {
        var data=new ExpeditionQuota();
        ListTag rows=tag.getList("entries",Tag.TAG_COMPOUND);
        for(int i=0;i<rows.size();i++){
            CompoundTag row=rows.getCompound(i);
            try {data.entries.put(UUID.fromString(row.getString("uuid")),
                    new Entry(row.getLong("day"),Math.max(0,row.getInt("used"))));}
            catch(IllegalArgumentException ignored){}
        }
        return data;
    }
    public static ExpeditionQuota get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(ExpeditionQuota::new,ExpeditionQuota::load),KEY);
    }
    private static long today(){return LocalDate.now(ZoneOffset.UTC).toEpochDay();}
    public int used(UUID id){
        Entry value=entries.get(id);
        return value!=null&&value.utcDay()==today()?value.used():0;
    }
    public int remaining(UUID id){
        int limit=DuskConfig.DAILY_RUNS.get();
        return limit==0?-1:Math.max(0,limit-used(id));
    }
    public boolean canStart(List<ServerPlayer> participants){
        return participants.stream().allMatch(player->remaining(player.getUUID())!=0);
    }
    public void consume(List<ServerPlayer> participants){
        // Must be called only after full-group validation and completed room generation.
        long day=today();
        for(ServerPlayer member:participants){
            UUID id=member.getUUID();
            entries.put(id,new Entry(day,used(id)+1));
        }
        setDirty();
    }
    public void reset(UUID player){
        if(entries.remove(player)!=null)setDirty();
    }
    public String status(ServerPlayer player){
        int remaining=remaining(player.getUUID());
        return remaining<0?"Unlimited daily entries":remaining+"/"+DuskConfig.DAILY_RUNS.get()+" entries remaining (UTC reset)";
    }
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider registries){
        ListTag list=new ListTag();
        long today=today();
        for(var value:entries.entrySet()){
            if(value.getValue().utcDay()!=today)continue;
            CompoundTag row=new CompoundTag();
            row.putString("uuid",value.getKey().toString());
            row.putLong("day",today);
            row.putInt("used",value.getValue().used());
            list.add(row);
        }
        tag.put("entries",list);
        return tag;
    }
}
