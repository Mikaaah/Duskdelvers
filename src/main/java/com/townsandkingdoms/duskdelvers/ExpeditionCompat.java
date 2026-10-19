package com.townsandkingdoms.duskdelvers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Runtime-optional integrations. Never link foreign classes in the class
 * signatures: if a mod is absent, the corresponding adapter is simply skipped.
 *
 * TNK Loot's public static create(Category, rank, source, type) is verified
 * against Mikaaah/TNK-Loot-Identification.
 */
public final class ExpeditionCompat {
    private static final Set<String> WARNED=new HashSet<>();
    private static final String[] PROVIDERS={
        "tnkloot","apotheosis","epicfight","irons_spellbooks","alexscaves",
        "cataclysm","twilightforest","aether","create","ars_nouveau",
        "ftbteams","ftbquests","puffish_skills"
    };

    private ExpeditionCompat(){}

    public static String status(){
        List<String> active=new ArrayList<>();
        List<String> absent=new ArrayList<>();
        for(String mod:PROVIDERS){
            if(ModList.get().isLoaded(mod))active.add(mod);
            else absent.add(mod);
        }
        return "Detected optional mods: "+String.join(", ",active)+
                " | Absent: "+String.join(", ",absent);
    }

    /** Called after the shared session's first successful boss completion. */
    public static boolean reward(ServerPlayer player,ExpeditionSessions.Run run){
        if(!run.member(player.getUUID()))return true;
        // Always publish a stable event for other addon mods to subscribe to.
        NeoForge.EVENT_BUS.post(new ExpeditionCompletedEvent(player,run.id(),run.tier()));
        if(DuskConfig.TNK_LOOT.get()&&DuskConfig.modAllowed("tnkloot")){
            addTnkLootTokens(player,run.tier());
        }
        if(DuskConfig.modAllowed("ftbquests")){
            String questId=DuskConfig.FTB_QUEST_IDS.get(run.tier()-1).get().trim();
            if(!questId.isBlank()&&questId.matches("[0-9A-Fa-f]{1,16}")){
                command(player,"ftbquests change_progress "+
                        player.getGameProfile().getName()+" complete "+questId,"ftbquests");
            }
        }
        if(DuskConfig.modAllowed("puffish_skills")){
            String category=DuskConfig.SKILLS_CATEGORY.get().trim();
            if(category.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")){
                int amount=DuskConfig.SKILLS_XP_PER_TIER.get()*run.tier();
                if(amount>0)command(player,"puffish_skills experience add "+
                        player.getGameProfile().getName()+" "+category+" "+amount,"puffish_skills");
            }
        }
        return true;
    }

    private static void command(ServerPlayer player,String command,String source){
        try{
            player.getServer().getCommands().performPrefixedCommand(
                    player.getServer().createCommandSourceStack().withPermission(4)
                            .withSuppressedOutput(),command);
        }catch(RuntimeException ex){
            warn(source,"Optional reward command was rejected: "+command,ex);
        }
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    private static void addTnkLootTokens(ServerPlayer player,int dungeonTier){
        try{
            Class<?> categoryClass=Class.forName("dev.mikaaah.tnkloot.config.TnkConfig$Category");
            Class<?> tokenClass=Class.forName("dev.mikaaah.tnkloot.loot.UnidentifiedWeaponItem");
            Class<?> selector=Class.forName("dev.mikaaah.tnkloot.loot.WeaponSelector");
            Class<?> progression=Class.forName("dev.mikaaah.tnkloot.progression.ProgressionService");

            int progressionRank=(int) progression.getMethod("getTier",ServerPlayer.class).invoke(null,player);
            // Respect TNK Loot's advancement progression, never grant a rank
            // the player has not unlocked merely by entering a harder portal.
            int rank=Math.max(0,progressionRank);
            Method pool=selector.getMethod("pool",int.class,categoryClass);
            Method create=tokenClass.getMethod("create",categoryClass,int.class,String.class,String.class);
            String[] choices={"WEAPON","BOW","HELMET","CHESTPLATE","LEGGINGS","BOOTS","TOOL","ACCESSORY"};
            List<Object> available=new ArrayList<>();
            for(String candidate:choices){
                Object category=Enum.valueOf((Class<? extends Enum>)categoryClass,candidate);
                Object selection=pool.invoke(null,rank,category);
                if(!(boolean)selection.getClass().getMethod("isEmpty").invoke(selection)){
                    available.add(category);
                }
            }
            if(available.isEmpty())return; // Never distribute impossible-to-identify tokens.
            int count=DuskConfig.UNIDENTIFIED_REWARDS.get();
            for(int i=0;i<count;i++){
                Object category=available.get(player.getRandom().nextInt(available.size()));
                Object result=create.invoke(null,category,rank,
                        "duskdelvers:guardian","expedition_tier_"+dungeonTier);
                if(result instanceof ItemStack stack&&!stack.isEmpty()){
                    player.getInventory().add(stack);
                    if(!stack.isEmpty())player.drop(stack,false);
                }
            }
            player.sendSystemMessage(Component.literal(
                    "TNK Loot: "+count+" unidentified expedition rewards, at your unlocked loot rank."));
        }catch(ReflectiveOperationException | RuntimeException ex){
            warn("tnkloot","TNK Loot reward bridge unavailable; normal expedition rewards remain intact",ex);
        }
    }

    private static void warn(String mod,String message,Throwable ex){
        if(!WARNED.add(mod+":"+message))return;
        if(ex==null)Duskdelvers.LOG.warn(message);
        else Duskdelvers.LOG.warn(message,ex);
    }

    /**
     * Addons can listen to this event on NeoForge.EVENT_BUS without Duskdelvers
     * depending on FTB, Apotheosis or any specific encounter provider.
     */
    public static final class ExpeditionCompletedEvent extends Event {
        private final ServerPlayer player;
        private final int runId;
        private final int tier;
        public ExpeditionCompletedEvent(ServerPlayer player,int runId,int tier){
            this.player=player;this.runId=runId;this.tier=tier;
        }
        public ServerPlayer player(){return player;}
        public int runId(){return runId;}
        public int tier(){return tier;}
    }

    /** Utility for optional environmental decorations. */
    public static net.minecraft.world.level.block.Block findBlock(String provider,String id,
            net.minecraft.world.level.block.Block fallback){
        if(!DuskConfig.modAllowed(provider))return fallback;
        try{
            ResourceLocation key=ResourceLocation.parse(id);
            if(BuiltInRegistries.BLOCK.containsKey(key))return BuiltInRegistries.BLOCK.get(key);
        }catch(RuntimeException ignored){}
        return fallback;
    }
}
