package com.townsandkingdoms.duskdelvers;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

/**
 * Durable server-owned natural-cavern expedition state: one party countdown,
 * one cache discovery milestone and one individual reward ledger.
 */
public final class ExpeditionSessions extends SavedData {
    public static final String PLAYER_RUN_ID="DuskdelversSessionId";
    private final Map<Integer,Run> runs=new HashMap<>();

    public static final class Run {
        private final ExpeditionLayout layout;
        private final List<UUID> members;
        private int seconds;
        private boolean discovered;
        private int discoveryTier;
        private boolean expired;
        private transient long lastSecond=-1;
        private final Set<UUID> rewarded=new HashSet<>();

        private Run(ExpeditionLayout layout,List<UUID> members,int seconds){
            this.layout=layout;
            this.members=List.copyOf(members);
            this.seconds=seconds;
        }

        public ExpeditionLayout layout(){return layout;}
        public int id(){return layout.id();}
        public int tier(){return layout.tier();}
        public List<UUID> members(){return members;}
        public int seconds(){return seconds;}
        public boolean discovered(){return discovered;}
        public int discoveryTier(){return discoveryTier>0?discoveryTier:tier();}
        public boolean expired(){return expired;}
        public boolean member(UUID id){return members.contains(id);}
        public boolean rewarded(UUID id){return rewarded.contains(id);}

        public String timeText(){return "%d:%02d".formatted(seconds/60,seconds%60);}
        private CompoundTag toTag(){
            CompoundTag tag=layout.toTag();
            ListTag list=new ListTag();
            for(UUID uuid:members){
                CompoundTag row=new CompoundTag();
                row.putString("uuid",uuid.toString());
                list.add(row);
            }
            tag.put("members",list);
            ListTag rewards=new ListTag();
            for(UUID uuid:rewarded) {
                CompoundTag row=new CompoundTag();
                row.putString("uuid",uuid.toString());
                rewards.add(row);
            }
            tag.put("rewarded",rewards);
            tag.putInt("seconds",seconds);
            tag.putBoolean("discovered",discovered);
            tag.putInt("discoveryTier",discoveryTier);
            tag.putBoolean("expired",expired);
            return tag;
        }
        private static Run fromTag(CompoundTag tag){
            List<UUID> ids=new ArrayList<>();
            ListTag list=tag.getList("members",Tag.TAG_COMPOUND);
            for(int i=0;i<list.size();i++){
                try{ids.add(UUID.fromString(list.getCompound(i).getString("uuid")));}
                catch(IllegalArgumentException ignored){}
            }
            Run run=new Run(ExpeditionLayout.fromTag(tag),ids,Math.max(0,tag.getInt("seconds")));
            // Legacy 0.3 sessions saved boss completion under "completed".
            run.discovered=tag.getBoolean("discovered")||tag.getBoolean("completed");
            run.discoveryTier=tag.getInt("discoveryTier");
            run.expired=tag.getBoolean("expired");
            ListTag rewards=tag.getList("rewarded",Tag.TAG_COMPOUND);
            for(int i=0;i<rewards.size();i++){
                try{run.rewarded.add(UUID.fromString(rewards.getCompound(i).getString("uuid")));}
                catch(IllegalArgumentException ignored){}
            }
            return run;
        }
    }

    private static ExpeditionSessions load(CompoundTag tag,HolderLookup.Provider provider){
        ExpeditionSessions store=new ExpeditionSessions();
        ListTag list=tag.getList("runs",Tag.TAG_COMPOUND);
        for(int i=0;i<list.size();i++){
            try{
                Run run=Run.fromTag(list.getCompound(i));
                if(run.id()>0 && !run.layout().rooms().isEmpty() && !run.members().isEmpty())
                    store.runs.put(run.id(),run);
            }catch(RuntimeException ignored){}
        }
        return store;
    }
    public static ExpeditionSessions get(MinecraftServer server){
        return server.overworld().getDataStorage().computeIfAbsent(
                new Factory<>(ExpeditionSessions::new,ExpeditionSessions::load),
                "duskdelvers_sessions");
    }

    public Run create(ExpeditionLayout layout,List<ServerPlayer> members){
        if(runs.containsKey(layout.id())) throw new IllegalStateException("Duplicate expedition ID");
        int seconds=DuskConfig.TIMERS_ENABLED.get()
                ?DuskConfig.TIER_MINUTES.get(layout.tier()-1).get()*60:-1;
        Run run=new Run(layout,members.stream().map(ServerPlayer::getUUID).toList(),seconds);
        runs.put(layout.id(),run);
        setDirty();
        return run;
    }

    public Run byId(int id){return runs.get(id);}
    public Run forPlayer(ServerPlayer player){
        var tag=player.getPersistentData();
        if(!tag.contains(PLAYER_RUN_ID,Tag.TAG_INT))return null;
        Run run=runs.get(tag.getInt(PLAYER_RUN_ID));
        return run!=null&&run.member(player.getUUID())?run:null;
    }
    /**
     * A run has only one milestone: its first discovered natural cave cache.
     * Its difficulty is the biome where the cache was found, not the gateway tier.
     */
    public boolean discover(Run run,int biomeTier){
        if(run.discovered||run.expired)return false;
        run.discovered=true;
        run.discoveryTier=Math.max(1,Math.min(5,biomeTier));
        if(run.seconds>=0)
            run.seconds=Math.max(run.seconds,DuskConfig.EXTRACTION_GRACE_MINUTES.get()*60);
        setDirty();
        return true;
    }

    /**
     * Called on each online member's server player tick. The monotonic
     * dimension clock ensures each run decrements at most once per second,
     * independent of its team size. Offline parties have no ticking members.
     */
    public boolean tickTime(Run run,long dimensionGameTime){
        if(run.expired||!DuskConfig.TIMERS_ENABLED.get())return false;
        if(run.seconds<0){
            run.seconds=DuskConfig.TIER_MINUTES.get(run.tier()-1).get()*60;
            setDirty();
        }
        long now=dimensionGameTime/20L;
        if(run.lastSecond==now)return run.seconds==0;
        if(run.lastSecond<0){
            run.lastSecond=now;
            return run.seconds==0;
        }
        run.lastSecond=now;
        if(run.seconds>0){
            run.seconds--;
            setDirty();
        }
        return run.seconds==0;
    }

    public void markRewarded(Run run,UUID player){
        if(run.member(player)&&run.rewarded.add(player))setDirty();
    }

    public void expire(Run run){
        if(run.expired)return;
        run.expired=true;
        run.seconds=0;
        setDirty();
    }

    public List<ServerPlayer> onlineMembers(MinecraftServer server,Run run){
        List<ServerPlayer> result=new ArrayList<>();
        for(UUID uuid:run.members()){
            ServerPlayer player=server.getPlayerList().getPlayer(uuid);
            if(player!=null&&player.serverLevel().dimension().equals(DuskCommands.CHAOS_CAVERNS)&&
                    player.getPersistentData().getInt(PLAYER_RUN_ID)==run.id())result.add(player);
        }
        return result;
    }

    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider provider){
        ListTag values=new ListTag();
        for(Run run:runs.values())values.add(run.toTag());
        tag.put("runs",values);
        return tag;
    }
}
