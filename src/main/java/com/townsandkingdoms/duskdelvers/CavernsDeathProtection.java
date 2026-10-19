package com.townsandkingdoms.duskdelvers;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.*;

/**
 * Dimension-local keepInventory, without changing Minecraft's global gamerule.
 * Snapshot before vanilla death processing and restore on PlayerEvent.Clone.
 * The expedition entry is consumed and the dead member leaves the active run.
 */
public final class CavernsDeathProtection {
    private record Snapshot(List<ItemStack> main,List<ItemStack> armor,List<ItemStack> offhand,
                            int experienceLevel,int totalExperience,float progress) {}
    private static final Map<UUID,Snapshot> PENDING=new HashMap<>();

    private CavernsDeathProtection() {}

    private static boolean inside(ServerPlayer player) {
        return player.serverLevel().dimension().equals(DuskCommands.CHAOS_CAVERNS);
    }
    private static List<ItemStack> copy(List<ItemStack> stacks) {
        return stacks.stream().map(ItemStack::copy).toList();
    }

    public static void onDeath(LivingDeathEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player)||!inside(player)||
                !DuskConfig.KEEP_INVENTORY.get())return;
        var inv=player.getInventory();
        PENDING.put(player.getUUID(),new Snapshot(copy(inv.items),copy(inv.armor),
                copy(inv.offhand),player.experienceLevel,player.totalExperience,player.experienceProgress));
    }

    public static void onDrops(LivingDropsEvent event) {
        if(event.getEntity() instanceof ServerPlayer player&&inside(player)&&
                DuskConfig.KEEP_INVENTORY.get()) {
            event.setCanceled(true);
        }
    }

    public static void onXp(LivingExperienceDropEvent event) {
        if(event.getEntity() instanceof ServerPlayer player&&inside(player)&&
                DuskConfig.KEEP_EXPERIENCE.get()) {
            event.setDroppedExperience(0);
        }
    }

    private static void restore(List<ItemStack> inventory,List<ItemStack> snapshot) {
        for(int i=0;i<Math.min(inventory.size(),snapshot.size());i++){
            inventory.set(i,snapshot.get(i).copy());
        }
    }

    public static void onClone(PlayerEvent.Clone event) {
        if(!event.isWasDeath()||!(event.getEntity() instanceof ServerPlayer next)||
                !(event.getOriginal() instanceof ServerPlayer original))return;
        if(!original.serverLevel().dimension().equals(DuskCommands.CHAOS_CAVERNS))return;
        Snapshot snapshot=PENDING.remove(original.getUUID());
        if(snapshot!=null&&DuskConfig.KEEP_INVENTORY.get()){
            var inv=next.getInventory();
            restore(inv.items,snapshot.main());
            restore(inv.armor,snapshot.armor());
            restore(inv.offhand,snapshot.offhand());
            inv.setChanged();
            if(DuskConfig.KEEP_EXPERIENCE.get()){
                next.experienceLevel=snapshot.experienceLevel();
                next.totalExperience=snapshot.totalExperience();
                next.experienceProgress=snapshot.progress();
            }
        }
        // Death counts as a failed entry. Clear the player's active run; team stays alive.
        next.getPersistentData().remove(ExpeditionRuntime.ACTIVE);
        next.getPersistentData().remove(ExpeditionSessions.PLAYER_RUN_ID);
        next.getPersistentData().remove("DuskdelversReturn");
        ExpeditionTimer.clear(next);
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        PENDING.remove(event.getEntity().getUUID());
    }
}
