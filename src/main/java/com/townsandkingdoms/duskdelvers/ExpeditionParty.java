package com.townsandkingdoms.duskdelvers;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Opt-in native expeditions parties, plus optional consent-gated FTB Teams.
 * No hard references to FTB classes are loaded without the mod present.
 */
public final class ExpeditionParty {
    private static final Map<UUID, UUID> OWNER_BY_MEMBER = new HashMap<>();
    private static final Map<UUID, LinkedHashSet<UUID>> MEMBERS = new HashMap<>();
    private static final Map<UUID, UUID> INVITES = new HashMap<>();
    private static final String FTB_READY = "DuskdelversFtbTeamReady";
    private static boolean ftbWarning;

    private ExpeditionParty() {}

    private static void message(ServerPlayer player,String message) {
        player.sendSystemMessage(Component.literal(message));
    }

    public static boolean invite(ServerPlayer leader, ServerPlayer target) {
        if (leader.getUUID().equals(target.getUUID())) return false;
        UUID owner=OWNER_BY_MEMBER.get(leader.getUUID());
        if(owner!=null && !owner.equals(leader.getUUID())){
            message(leader,"Only the party leader may invite players.");
            return false;
        }
        if(OWNER_BY_MEMBER.containsKey(target.getUUID())){
            message(leader,"This player already belongs to a Duskdelvers party.");
            return false;
        }
        Set<UUID> group=MEMBERS.get(leader.getUUID());
        if(group!=null && group.size()>=DuskConfig.MAX_PARTY_SIZE.get()){
            message(leader,"Your expedition party is full.");
            return false;
        }
        INVITES.put(target.getUUID(),leader.getUUID());
        message(target,leader.getGameProfile().getName()+" invited you to a Duskdelvers party. Use /duskdelvers team accept.");
        message(leader,"Invitation sent to "+target.getGameProfile().getName()+".");
        return true;
    }

    public static boolean accept(ServerPlayer player){
        UUID leaderId=INVITES.remove(player.getUUID());
        if(leaderId==null){message(player,"You have no pending Duskdelvers invitation.");return false;}
        ServerPlayer leader=player.getServer().getPlayerList().getPlayer(leaderId);
        if(leader==null){message(player,"The party leader is offline.");return false;}
        if(OWNER_BY_MEMBER.containsKey(player.getUUID())){
            message(player,"Leave your current party before accepting another invitation.");return false;
        }
        LinkedHashSet<UUID> group=MEMBERS.computeIfAbsent(leaderId,k->new LinkedHashSet<>());
        group.add(leaderId);
        if(group.size()>=DuskConfig.MAX_PARTY_SIZE.get()){
            message(player,"The party has reached the server's maximum size.");return false;
        }
        group.add(player.getUUID());
        OWNER_BY_MEMBER.put(leaderId,leaderId);
        OWNER_BY_MEMBER.put(player.getUUID(),leaderId);
        message(player,"Joined "+leader.getGameProfile().getName()+"'s expedition party.");
        message(leader,player.getGameProfile().getName()+" joined your party.");
        return true;
    }

    public static boolean leave(ServerPlayer player){
        UUID leader=OWNER_BY_MEMBER.remove(player.getUUID());
        if(leader==null){message(player,"You are not in a native expedition party.");return false;}
        LinkedHashSet<UUID> group=MEMBERS.get(leader);
        if(group==null)return true;
        if(leader.equals(player.getUUID())){
            for(UUID member:group)OWNER_BY_MEMBER.remove(member);
            MEMBERS.remove(leader);
            message(player,"You dissolved your expedition party.");
        }else{
            group.remove(player.getUUID());
            if(group.size()==1){
                OWNER_BY_MEMBER.remove(leader);
                MEMBERS.remove(leader);
            }
            message(player,"Left the expedition party.");
        }
        return true;
    }

    public static void toggleFtbReady(ServerPlayer player){
        boolean ready=!player.getPersistentData().getBoolean(FTB_READY);
        player.getPersistentData().putBoolean(FTB_READY,ready);
        message(player,"FTB Teams expedition matchmaking "+(ready?"enabled":"disabled")+
                ". Only nearby, mutually opted-in FTB teammates will enter together.");
    }

    public static String status(ServerPlayer player) {
        UUID leader=OWNER_BY_MEMBER.get(player.getUUID());
        if(leader==null)return "No native party. FTB Teams opt-in: "+player.getPersistentData().getBoolean(FTB_READY);
        Set<UUID> members=MEMBERS.get(leader);
        return "Party: "+(members==null?0:members.size())+" members. You are "+
                (leader.equals(player.getUUID())?"leader":"member")+
                ". FTB Teams opt-in: "+player.getPersistentData().getBoolean(FTB_READY);
    }

    public static List<ServerPlayer> participants(ServerPlayer leader){
        LinkedHashMap<UUID,ServerPlayer> selected=new LinkedHashMap<>();
        selected.put(leader.getUUID(),leader);
        MinecraftServer server=leader.getServer();
        UUID owner=OWNER_BY_MEMBER.get(leader.getUUID());
        if(owner!=null&&!owner.equals(leader.getUUID())){
            message(leader,"Your party leader must initiate the expedition.");
            return List.of();
        }
        Set<UUID> party=MEMBERS.get(leader.getUUID());
        if(party!=null){
            for(UUID id:party){
                ServerPlayer member=server.getPlayerList().getPlayer(id);
                if(member==null){
                    message(leader,"All native party members must be online.");
                    return List.of();
                }
                selected.put(id,member);
            }
        }
        if(DuskConfig.FTB_TEAMS.get() && DuskConfig.modAllowed("ftbteams") &&
                leader.getPersistentData().getBoolean(FTB_READY)){
            Optional<String> team=ftbTeamKey(leader);
            if(team.isPresent()){
                for(ServerPlayer online:server.getPlayerList().getPlayers()){
                    if(!online.getPersistentData().getBoolean(FTB_READY))continue;
                    if(online.serverLevel()!=leader.serverLevel()||
                            online.distanceToSqr(leader)>Math.pow(DuskConfig.PARTY_RADIUS.get(),2))continue;
                    if(team.equals(ftbTeamKey(online)))selected.putIfAbsent(online.getUUID(),online);
                }
            }
        }
        if(selected.size()>DuskConfig.MAX_PARTY_SIZE.get()){
            message(leader,"Too many players in the party ("+selected.size()+
                    "). Maximum is "+DuskConfig.MAX_PARTY_SIZE.get()+".");
            return List.of();
        }
        double radiusSq=Math.pow(DuskConfig.PARTY_RADIUS.get(),2);
        for(ServerPlayer member:selected.values()){
            if(member.serverLevel()!=leader.serverLevel()||
                    member.distanceToSqr(leader)>radiusSq){
                message(leader,member.getGameProfile().getName()+
                        " must gather within "+DuskConfig.PARTY_RADIUS.get()+" blocks.");
                return List.of();
            }
            if(member.getPersistentData().contains(ExpeditionRuntime.ACTIVE)){
                message(leader,member.getGameProfile().getName()+" is already in an expedition.");
                return List.of();
            }
        }
        return List.copyOf(selected.values());
    }

    /** FTB Teams API verified against the user's TNK Loot TeamResolver adapter. */
    private static Optional<String> ftbTeamKey(ServerPlayer player){
        try{
            Class<?> clazz=Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI");
            Object api=clazz.getMethod("api").invoke(null);
            Class<?> apiType=Class.forName("dev.ftb.mods.ftbteams.api.FTBTeamsAPI$API");
            if(!(boolean)apiType.getMethod("isManagerLoaded").invoke(api))return Optional.empty();
            Object manager=apiType.getMethod("getManager").invoke(api);
            Class<?> managerClass=Class.forName("dev.ftb.mods.ftbteams.api.TeamManager");
            Method lookup=managerClass.getMethod("getTeamForPlayerID",UUID.class);
            Object result=lookup.invoke(manager,player.getUUID());
            if(!(result instanceof Optional<?> optional)||optional.isEmpty())return Optional.empty();
            Class<?> teamClass=Class.forName("dev.ftb.mods.ftbteams.api.Team");
            Object id=teamClass.getMethod("getId").invoke(optional.get());
            return Optional.of("ftbteam:"+id);
        }catch(ReflectiveOperationException | RuntimeException ex){
            if(!ftbWarning){
                ftbWarning=true;
                Duskdelvers.LOG.warn("FTB Teams optional API is unavailable; native Duskdelvers parties remain usable",ex);
            }
            return Optional.empty();
        }
    }
}
