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
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.tickCount % 20 != 0) return;
        ServerLevel level=player.serverLevel();
        if (!level.dimension().equals(DuskCommands.CHAOS_CAVERNS)) return;
        CompoundTag tag=player.getPersistentData().getCompound(ACTIVE);
        if (!tag.contains("rooms")) return;
        ExpeditionLayout layout=ExpeditionLayout.fromTag(tag);
        List<BlockPos> rooms=layout.rooms();
        if (rooms.size()<2) return;
        int activated=tag.getInt("activated");
        int cleared=tag.getInt("cleared");

        for (int i=1;i<rooms.size();i++) {
            int mask=1<<i;
            if ((cleared & mask)!=0) continue;
            BlockPos center=rooms.get(i);
            // Only process rooms around the participant, never the entire level.
            if (player.blockPosition().distSqr(center)>400) continue;
            if (!level.hasChunkAt(center)) continue;
            if ((activated & mask)==0) {
                spawnRoom(level,layout,i);
                activated|=mask;
                tag.putInt("activated",activated);
                player.sendSystemMessage(Component.literal(
                        (i==rooms.size()-1 ? "A dungeon guardian approaches! " : "Ambush! ")
                        +"Tier "+layout.tier()+" • Room "+i+"/"+(rooms.size()-1)));
            }

            AABB bounds=new AABB(center.getX()-9,center.getY(),center.getZ()-9,center.getX()+10,center.getY()+7,center.getZ()+10);
            final int roomIndex=i;
            List<Mob> alive=level.getEntitiesOfClass(Mob.class,bounds, mob ->
                    mob.isAlive() && mob.getPersistentData().getInt(MOB_RUN)==layout.id()
                    && mob.getPersistentData().getInt(MOB_ROOM)==roomIndex);
            if (!alive.isEmpty()) continue;
            cleared|=mask;
            tag.putInt("cleared",cleared);
            player.giveExperiencePoints(layout.tier()*4);
            if (i==rooms.size()-1 && !tag.getBoolean("completed")) {
                tag.putBoolean("completed",true);
                if (DuskConfig.BOSS_REWARDS.get()) DungeonBuilder.bossReward(level,center,layout.tier(),layout.id());
                player.giveExperiencePoints(layout.tier()*25);
                player.sendSystemMessage(Component.literal(
                    "Expedition complete! Tier "+layout.tier()+" guardian defeated. Claim the reward chest and use your compass or /duskdelvers exit."));
            } else {
                player.sendSystemMessage(Component.literal("Room "+i+" cleared. Continue deeper."));
            }
        }
    }

    private static void spawnRoom(ServerLevel level,ExpeditionLayout layout,int roomIndex) {
        BlockPos center=layout.rooms().get(roomIndex);
        boolean boss=roomIndex==layout.rooms().size()-1;
        int tier=layout.tier();
        Random rng=new Random(level.getSeed() ^ ((long)layout.id()*97161283L) ^ roomIndex);
        int count=boss?1:Math.min(DuskConfig.MAX_ROOM_ENEMIES.get(),2+tier+(roomIndex%2));
        for (int n=0;n<count;n++) {
            EntityType<?> type=boss?(tier>=3 ? EntityType.RAVAGER : EntityType.WITHER_SKELETON)
                    : EncounterProfiles.pick(tier,rng);
            Entity entity=type.create(level);
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
                double factor=1.0+0.35*(tier-1);
                if (boss) factor*=2.5;
                else if (n==0 && tier>=3 && roomIndex%2==0) factor*=1.5;
                AttributeInstance hp=mob.getAttribute(Attributes.MAX_HEALTH);
                if (hp!=null) {
                    hp.setBaseValue(Math.min(500,hp.getBaseValue()*factor));
                    mob.setHealth(mob.getMaxHealth());
                }
                AttributeInstance damage=mob.getAttribute(Attributes.ATTACK_DAMAGE);
                if (damage!=null) damage.setBaseValue(Math.min(35,damage.getBaseValue()*(1+0.2*(tier-1))));
            }
            level.addFreshEntity(mob);
        }
    }

    public static void clonePlayer(PlayerEvent.Clone event) {
        CompoundTag original=event.getOriginal().getPersistentData();
        CompoundTag target=event.getEntity().getPersistentData();
        for (String key:new String[]{"DuskdelversReturn",ACTIVE,"DuskdelversCompassTier"}) {
            if (original.contains(key)) target.put(key,original.get(key).copy());
        }
    }
}
