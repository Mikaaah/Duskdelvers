package com.townsandkingdoms.duskdelvers;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Random;

/** Server-side, room-scoped mob spawning, tracking, completion and reward logic. */
public final class ExpeditionRuntime {
    public static final String ACTIVE = "DuskdelversActive";
    private static final String MOB_RUN = "DuskdelversRun";
    private static final String MOB_ROOM = "DuskdelversRoom";
    private ExpeditionRuntime() {}

    public static void tick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        if(player.tickCount%20!=0)return;
        ServerLevel level=player.serverLevel();
        if(!level.dimension().equals(DuskCommands.CHAOS_CAVERNS))return;

        ExpeditionSessions sessions=ExpeditionSessions.get(player.getServer());
        ExpeditionSessions.Run run=sessions.forPlayer(player);
        if(run==null){
            if(player.getPersistentData().contains(ExpeditionSessions.PLAYER_RUN_ID)){
                player.sendSystemMessage(Component.literal(
                        "Your expedition session could not be found. Returning to safety."));
                DuskCommands.returnPlayer(player);
                return;
            }
            // The 0.2.2 arrival outpost keeps its independent dimension timer.
            ExpeditionTimer.tick(player);
            return;
        }
        if(run.expired()){
            DuskCommands.returnPlayer(player);
            return;
        }

        int before=run.seconds();
        if(sessions.tickTime(run,level.getGameTime())){
            sessions.expire(run);
            for(ServerPlayer member:sessions.onlineMembers(player.getServer(),run)){
                member.sendSystemMessage(Component.literal(
                        "The expedition time has expired. Your inventory is safe; returning to the entry point."));
                DuskCommands.returnPlayer(member);
            }
            return;
        }
        int seconds=run.seconds();
        if(seconds>=0&&DuskConfig.DISPLAY_TIMER.get()){
            net.minecraft.ChatFormatting color=seconds<=60?net.minecraft.ChatFormatting.RED:
                    seconds<=300?net.minecraft.ChatFormatting.YELLOW:net.minecraft.ChatFormatting.LIGHT_PURPLE;
            player.displayClientMessage(Component.literal(
                    "Duskdelvers • Team "+run.members().size()+" • "+run.timeText()+" remaining")
                    .withStyle(color),true);
        }
        if(before!=seconds&&(seconds==300||seconds==60||seconds==10)){
            for(ServerPlayer member:sessions.onlineMembers(player.getServer(),run))
                member.sendSystemMessage(Component.literal(
                        "Expedition warning: "+run.timeText()+" until party extraction."));
        }

        // The boss reward is per player, with a SavedData claim ledger. Reconnecting
        // during the extraction window does not lose a claim or duplicate rewards.
        if(run.completed()&&!run.rewarded(player.getUUID())){
            if(ExpeditionCompat.reward(player,run)){
                sessions.markRewarded(run,player.getUUID());
            }
        }

        if(run.completed())return;
        var rooms=run.layout().rooms();
        for(int i=1;i<rooms.size();i++){
            int mask=1<<i;
            if((run.cleared()&mask)!=0)continue;
            BlockPos center=rooms.get(i);
            if(player.blockPosition().distSqr(center)>400 || !level.hasChunkAt(center))continue;
            if((run.activated()&mask)==0){
                int spawned=spawnRoom(level,run.layout(),i,run.members().size());
                if(spawned==0)continue;
                sessions.activate(run,i);
                for(ServerPlayer member:sessions.onlineMembers(player.getServer(),run))
                    member.sendSystemMessage(Component.literal(
                            (i==rooms.size()-1?"A dungeon guardian approaches! ":"Ambush! ")+
                            "Tier "+run.tier()+" • Room "+i+"/"+(rooms.size()-1)));
            }
            int room=i;
            AABB bounds=new AABB(center.getX()-18,center.getY()-2,center.getZ()-18,
                    center.getX()+19,center.getY()+12,center.getZ()+19);
            List<Mob> alive=level.getEntitiesOfClass(Mob.class,bounds,mob->
                    mob.isAlive()&&mob.getPersistentData().getInt(MOB_RUN)==run.id()&&
                    mob.getPersistentData().getInt(MOB_ROOM)==room);
            if(!alive.isEmpty())continue;
            if(!sessions.clear(run,i))continue;
            for(ServerPlayer member:sessions.onlineMembers(player.getServer(),run)){
                member.giveExperiencePoints(run.tier()*4);
            }
            if(i==rooms.size()-1&&sessions.complete(run)){
                if(DuskConfig.BOSS_REWARDS.get())
                    DungeonBuilder.bossReward(level,center,run.tier(),run.id());
                PortalFrames.placeReturnGateway(level,center);
                for(ServerPlayer member:sessions.onlineMembers(player.getServer(),run)){
                    member.giveExperiencePoints(run.tier()*25);
                    member.sendSystemMessage(Component.literal(
                            "Team expedition complete! Claim your personal rewards, loot the boss chest, and extract. "+
                            (run.seconds()<0?"":run.timeText()+" remaining.")));
                    if(ExpeditionCompat.reward(member,run)){
                        sessions.markRewarded(run,member.getUUID());
                    }
                }
            }else{
                for(ServerPlayer member:sessions.onlineMembers(player.getServer(),run))
                    member.sendSystemMessage(Component.literal(
                            "Room "+i+" cleared for your expedition team. Continue deeper."));
            }
        }
    }

    private static int spawnRoom(ServerLevel level,ExpeditionLayout layout,int roomIndex,int partySize) {
        BlockPos center=layout.rooms().get(roomIndex);
        boolean boss=roomIndex==layout.rooms().size()-1;
        int tier=layout.tier();
        Random rng=new Random(level.getSeed() ^ ((long)layout.id()*97161283L) ^ roomIndex);
        int count=boss?1:Math.min(DuskConfig.MAX_ROOM_ENEMIES.get(),2+tier+(roomIndex%2)+Math.max(0,partySize-1)*2);
        int spawned=0;
        for (int n=0;n<count;n++) {
            EntityType<?> type=boss?(tier>=3 ? EntityType.RAVAGER : EntityType.WITHER_SKELETON)
                    : EncounterProfiles.pick(tier,rng);
            // Some third-party entities assume their native dimension/arena.
            // A broken optional constructor must never kill the expedition tick.
            Entity entity;
            try {
                entity=type.create(level);
            } catch(RuntimeException ex) {
                Duskdelvers.LOG.warn("Skipping incompatible expedition entity {}",type,ex);
                entity=null;
            }
            Mob mob = entity instanceof Mob candidate ? candidate : EntityType.ZOMBIE.create(level);
            if (mob == null) continue;
            int dx=boss?0:(n%3-1)*2;
            int dz=boss?0:((n/3)%3-1)*2;
            mob.moveTo(center.getX()+dx+0.5D,center.getY()+1D,
                    center.getZ()+dz+0.5D,rng.nextFloat()*360.0F,0);
            mob.getPersistentData().putInt(MOB_RUN,layout.id());
            mob.getPersistentData().putInt(MOB_ROOM,roomIndex);
            mob.setPersistenceRequired();
            if (boss) {
                mob.setCustomName(Component.literal("Guardian of the Depths • Tier "+tier));
                mob.setCustomNameVisible(true);
                mob.setGlowingTag(true);
            } else if (n==0 && tier>=3 && roomIndex%2==0) {
                mob.setCustomName(Component.literal("Abyssal Elite • Tier "+tier));
                mob.setCustomNameVisible(true);
            }
            if (DuskConfig.MOB_SCALING.get()) {
                double factor=(1.0+0.35*(tier-1))*(1.0+0.22*Math.max(0,partySize-1));
                if (boss) factor*=2.5;
                else if (n==0 && tier>=3 && roomIndex%2==0) factor*=1.5;
                AttributeInstance hp=mob.getAttribute(Attributes.MAX_HEALTH);
                if (hp!=null) {
                    hp.setBaseValue(Math.min(500,hp.getBaseValue()*factor));
                    mob.setHealth(mob.getMaxHealth());
                }
                AttributeInstance damage=mob.getAttribute(Attributes.ATTACK_DAMAGE);
                if (damage!=null) damage.setBaseValue(Math.min(35,damage.getBaseValue()*(1+0.2*(tier-1))*(1+0.08*Math.max(0,partySize-1))));
            }
            if(level.addFreshEntity(mob))spawned++;
        }
        return spawned;
    }

    public static void clonePlayer(PlayerEvent.Clone event) {
        CompoundTag original=event.getOriginal().getPersistentData();
        CompoundTag target=event.getEntity().getPersistentData();
        for (String key:new String[]{"DuskdelversReturn",ACTIVE,"DuskdelversCompassTier",
                ExpeditionTimer.REMAINING_SECONDS,"DuskdelversTimerTier",
                ExpeditionSessions.PLAYER_RUN_ID,"DuskdelversFtbTeamReady"}) {
            if (original.contains(key)) target.put(key,original.get(key).copy());
        }
    }
}
