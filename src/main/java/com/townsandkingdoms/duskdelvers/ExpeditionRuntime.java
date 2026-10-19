package com.townsandkingdoms.duskdelvers;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Random;

/**
 * Structure-free cave expeditions: world biome controls danger, native mobs
 * scale gently, optional fauna may roam near players and natural caches trigger
 * one-time expedition discovery milestones.
 */
public final class ExpeditionRuntime {
    public static final String ACTIVE="DuskdelversActive"; // Legacy 0.3 player NBT.
    private static final String SCALED="DuskdelversBiomeScaled";
    private static final String SPAWNED="DuskdelversOptionalSpawn";

    private ExpeditionRuntime(){}

    public static void tick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        if(player.tickCount%20!=0)return;
        ServerLevel level=player.serverLevel();
        if(!level.dimension().equals(DuskCommands.CHAOS_CAVERNS))return;

        ExpeditionSessions sessions=ExpeditionSessions.get(player.getServer());
        ExpeditionSessions.Run run=sessions.forPlayer(player);
        if(run==null){
            if(player.getPersistentData().contains(ExpeditionSessions.PLAYER_RUN_ID)){
                player.sendSystemMessage(Component.literal(
                        "Your saved cavern expedition is no longer available. Returning to safety."));
                DuskCommands.returnPlayer(player);
                return;
            }
            // Teleported into dimension from another mod/command; use standard outpost timer.
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
                        "The cavern expedition has expired. Your inventory is safe; extracting."));
                DuskCommands.returnPlayer(member);
            }
            return;
        }

        var biome=level.getBiome(player.blockPosition());
        int danger=CavernBiomes.tier(biome);
        if(DuskConfig.DISPLAY_TIMER.get()){
            var color=danger>=5?net.minecraft.ChatFormatting.RED:
                    danger>=4?net.minecraft.ChatFormatting.LIGHT_PURPLE:
                    danger>=3?net.minecraft.ChatFormatting.GOLD:
                    net.minecraft.ChatFormatting.GREEN;
            String timer=run.seconds()<0?"∞":run.timeText();
            player.displayClientMessage(Component.literal(
                    CavernBiomes.name(biome)+" • Danger "+danger+"/5 • "+
                    timer+" • Party "+run.members().size()).withStyle(color),true);
        }
        if(before!=run.seconds()&&(run.seconds()==300||run.seconds()==60||run.seconds()==10)){
            for(ServerPlayer member:sessions.onlineMembers(player.getServer(),run))
                member.sendSystemMessage(Component.literal(
                        "Cavern expedition warning: "+run.timeText()+" until team extraction."));
        }

        if(run.completed()&&!run.rewarded(player.getUUID())&&ExpeditionCompat.reward(player,run)){
            sessions.markRewarded(run,player.getUUID());
        }

        if(DuskConfig.MOB_SCALING.get())scaleNearbyVanillaMobs(player,danger,run.members().size());
        if(player.tickCount%600==0&&DuskConfig.OPTIONAL_MOBS.get())spawnAmbientOptional(player,danger);
    }

    private static void scaleNearbyVanillaMobs(ServerPlayer player,int danger,int partySize){
        ServerLevel level=player.serverLevel();
        AABB area=player.getBoundingBox().inflate(26,12,26);
        List<Mob> mobs=level.getEntitiesOfClass(Mob.class,area,mob->
                mob.isAlive()&&!mob.getPersistentData().getBoolean(SCALED));
        int processed=0;
        for(Mob mob:mobs){
            if(processed++>=20)break;
            // Leave foreign Epic Fight/Cataclysm/Iron's spells boss attributes alone.
            if(!BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getNamespace().equals("minecraft"))continue;
            mob.getPersistentData().putBoolean(SCALED,true);
            var health=mob.getAttribute(Attributes.MAX_HEALTH);
            if(health!=null){
                health.setBaseValue(Math.min(120,health.getBaseValue()*
                        (1+0.20*(danger-1))*(1+0.10*Math.max(0,partySize-1))));
                mob.setHealth(mob.getMaxHealth());
            }
            var attack=mob.getAttribute(Attributes.ATTACK_DAMAGE);
            if(attack!=null){
                attack.setBaseValue(Math.min(35,attack.getBaseValue()*(1+0.15*(danger-1))));
            }
        }
    }

    private static void spawnAmbientOptional(ServerPlayer player,int danger){
        ServerLevel level=player.serverLevel();
        var rng=player.getRandom();
        long present=level.getEntitiesOfClass(Mob.class,
                player.getBoundingBox().inflate(32,16,32),
                m->m.getPersistentData().getBoolean(SPAWNED)).size();
        if(present>=6)return;

        EntityType<?> type=EncounterProfiles.pickOptional(danger,new Random(rng.nextLong()));
        if(type==null)return;
        for(int tries=0;tries<8;tries++){
            int dx= (rng.nextInt(17)+13)*(rng.nextBoolean()?1:-1);
            int dz= (rng.nextInt(17)+13)*(rng.nextBoolean()?1:-1);
            int x=player.blockPosition().getX()+dx;
            int z=player.blockPosition().getZ()+dz;
            int y=player.blockPosition().getY()+rng.nextInt(11)-5;
            BlockPos candidate=new BlockPos(x,y,z);
            if(!level.hasChunkAt(candidate))continue;
            for(int down=0;down<10;down++){
                BlockPos pos=candidate.below(down);
                if(!level.getBlockState(pos).isAir()||
                        !level.getBlockState(pos.above()).isAir()||
                        !level.getBlockState(pos.below()).isSolidRender(level,pos.below()))continue;
                try{
                    Entity entity=type.create(level);
                    if(!(entity instanceof Mob mob))return;
                    mob.moveTo(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5,
                            rng.nextFloat()*360,0);
                    mob.getPersistentData().putBoolean(SPAWNED,true);
                    if(level.addFreshEntity(mob))return;
                }catch(RuntimeException ex){
                    Duskdelvers.LOG.warn("Optional cave mob {} could not spawn safely",type,ex);
                }
                return;
            }
        }
    }

    /** First discovery of a natural worldgen cache grants one optional team milestone. */
    public static void onCacheOpened(PlayerInteractEvent.RightClickBlock event){
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        if(!player.serverLevel().dimension().equals(DuskCommands.CHAOS_CAVERNS))return;
        if(!(player.serverLevel().getBlockEntity(event.getPos()) instanceof BarrelBlockEntity barrel))return;
        var key=barrel.getLootTable();
        if(key==null||!key.location().getNamespace().equals("duskdelvers")||
                !key.location().getPath().startsWith("chests/cavern_cache_tier_"))return;

        var sessions=ExpeditionSessions.get(player.getServer());
        var run=sessions.forPlayer(player);
        if(run==null||run.expired()||!sessions.discover(run))return;
        for(ServerPlayer member:sessions.onlineMembers(player.getServer(),run)){
            member.sendSystemMessage(Component.literal(
                    player.getGameProfile().getName()+" discovered a natural cavern cache! "+
                    "Team milestone rewards have been unlocked. Continue exploring."));
            if(ExpeditionCompat.reward(member,run))sessions.markRewarded(run,member.getUUID());
        }
    }

    public static void clonePlayer(PlayerEvent.Clone event){
        var original=event.getOriginal().getPersistentData();
        var target=event.getEntity().getPersistentData();
        for(String key:new String[]{"DuskdelversReturn",ACTIVE,"DuskdelversCompassTier",
                ExpeditionTimer.REMAINING_SECONDS,"DuskdelversTimerTier",
                ExpeditionSessions.PLAYER_RUN_ID,"DuskdelversFtbTeamReady"}){
            if(original.contains(key))target.put(key,original.get(key).copy());
        }
    }
}
