package com.townsandkingdoms.duskdelvers;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import java.util.List;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Set;

public final class DuskCommands {
    public static final ResourceKey<Level> CHAOS_CAVERNS = ResourceKey.create(
            Registries.DIMENSION,ResourceLocation.fromNamespaceAndPath("duskdelvers","chaos_caverns"));
    private static final String RETURN_TAG = "DuskdelversReturn";
    private DuskCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("duskdelvers")
                .then(Commands.literal("enter").executes(ctx -> enter(ctx.getSource())))
                .then(Commands.literal("start")
                        .then(Commands.argument("tier",IntegerArgumentType.integer(1,5))
                                .executes(ctx -> start(ctx.getSource(),IntegerArgumentType.getInteger(ctx,"tier")))))
                .then(Commands.literal("portal")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("activate")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> activatePortal(ctx.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(ctx, "pos"))))))
                .then(Commands.literal("team")
                    .then(Commands.literal("invite")
                        .then(Commands.argument("player",StringArgumentType.word())
                            .executes(ctx -> teamInvite(ctx.getSource(),StringArgumentType.getString(ctx,"player")))))
                    .then(Commands.literal("accept").executes(ctx -> teamAccept(ctx.getSource())))
                    .then(Commands.literal("leave").executes(ctx -> teamLeave(ctx.getSource())))
                    .then(Commands.literal("ready").executes(ctx -> teamReady(ctx.getSource())))
                    .then(Commands.literal("status").executes(ctx -> teamStatus(ctx.getSource()))))
                .then(Commands.literal("entries").executes(ctx -> entries(ctx.getSource())))
                .then(Commands.literal("exit").executes(ctx -> exit(ctx.getSource())))
                .then(Commands.literal("time").executes(ctx -> time(ctx.getSource())))
                .then(Commands.literal("limits").executes(ctx -> limits(ctx.getSource())))
                .then(Commands.literal("status").executes(ctx -> status(ctx.getSource()))));
    }

    private static int activatePortal(CommandSourceStack source, BlockPos frame) {
        boolean success = PortalFrames.activate(source.getLevel(), frame);
        if (success) source.sendSuccess(() -> Component.literal("Expedition portal activated."), false);
        else source.sendFailure(Component.literal("No complete player-made Duskdelvers frame at that position."));
        return success ? 1 : 0;
    }

    /**
     * Replace the saved return position with a safe point beside the entry
     * gateway. This avoids re-entering the same portal after extraction.
     */
    public static void adjustPortalReturn(ServerPlayer player, ServerLevel original,
                                          BlockPos portal, Direction.Axis axis,
                                          double x, double y, double z, Vec3 velocity) {
        CompoundTag data = player.getPersistentData().getCompound(RETURN_TAG);
        if (!data.contains("dimension")) return;
        double speed = axis == Direction.Axis.X ? velocity.z : velocity.x;
        double plane = (axis == Direction.Axis.X ? portal.getZ() : portal.getX()) + 0.5;
        double currentNormal = axis == Direction.Axis.X ? z : x;
        double preferred = speed > 0.01 ? -1.0 : speed < -0.01 ? 1.0
                : currentNormal < plane ? -1.0 : 1.0;

        for (double side : new double[]{preferred, -preferred}) {
            double targetX = axis == Direction.Axis.X ? x : plane + side * 2.5;
            double targetZ = axis == Direction.Axis.X ? plane + side * 2.5 : z;
            BlockPos feet = BlockPos.containing(targetX, y, targetZ);
            if (original.getBlockState(feet).isAir()
                    && original.getBlockState(feet.above()).isAir()
                    && !original.getBlockState(feet.below()).isAir()) {
                data.putDouble("x", targetX);
                data.putDouble("y", y);
                data.putDouble("z", targetZ);
                return;
            }
        }
        // Fallback: original position with a cooldown rather than an unsafe teleport.
    }

    private static int enter(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player=source.getPlayerOrException();
        if (player.serverLevel().dimension().equals(CHAOS_CAVERNS)) {
            player.sendSystemMessage(Component.literal("Already inside the Chaos Caverns."));
            return 0;
        }
        ServerLevel destination=source.getServer().getLevel(CHAOS_CAVERNS);
        if (destination==null) {
            player.sendSystemMessage(Component.literal("Chaos Caverns dimension is not loaded; check your datapacks."));
            return 0;
        }
        rememberOrigin(player);
        StarterOutpost.ensureCreated(destination);
        player.teleportTo(destination,0.5,72,0.5,Set.of(),player.getYRot(),player.getXRot());
        ExpeditionTimer.start(player, 0);
        player.sendSystemMessage(Component.literal("Entered the original arrival outpost. Use /duskdelvers start <1-5> for a generated dungeon."));
        return 1;
    }

    private static int start(CommandSourceStack source,int tier) throws CommandSyntaxException {
        return startPlayer(source.getPlayerOrException(),tier) ? 1 : 0;
    }

    public static boolean startPlayer(ServerPlayer player,int tier) {
        if (!DuskConfig.EXPEDITIONS_ENABLED.get()) {
            player.sendSystemMessage(Component.literal("Expeditions are disabled by the server config."));
            return false;
        }
        if (tier < 1 || tier > 5) return false;
        ServerLevel dungeon=player.getServer().getLevel(CHAOS_CAVERNS);
        if (dungeon==null) {
            player.sendSystemMessage(Component.literal("Chaos Caverns dimension is missing. Check datapack errors."));
            return false;
        }
        List<ServerPlayer> members=ExpeditionParty.participants(player);
        if(members.isEmpty()) return false;
        ExpeditionQuota quota=ExpeditionQuota.get(player.getServer());
        if(!quota.canStart(members)){
            for(ServerPlayer member:members){
                if(quota.remaining(member.getUUID())==0)
                    player.sendSystemMessage(Component.literal(member.getGameProfile().getName()
                            +" has used all expedition entries for today."));
            }
            return false;
        }
        int id=ExpeditionIndex.get(player.getServer()).allocate();
        ExpeditionLayout layout=ExpeditionLayout.generate(id,tier,dungeon.getSeed());
        if(layout.rooms().size()<3){
            player.sendSystemMessage(Component.literal("Failed to generate an expedition route."));
            return false;
        }
        try {
            DungeonBuilder.build(dungeon,layout);
        } catch(RuntimeException ex) {
            Duskdelvers.LOG.error("Failed constructing expedition {}",id,ex);
            player.sendSystemMessage(Component.literal("Dungeon generation failed. No entries were charged."));
            return false;
        }

        // Store the session before charging entries. All members are committed as a group.
        ExpeditionSessions sessions=ExpeditionSessions.get(player.getServer());
        ExpeditionSessions.Run run=sessions.create(layout,members);
        quota.consume(members);
        var spawn=layout.rooms().get(0);
        for(ServerPlayer member:members){
            if(!member.serverLevel().dimension().equals(CHAOS_CAVERNS))rememberOrigin(member);
            member.getPersistentData().put(ExpeditionRuntime.ACTIVE,layout.toTag());
            member.getPersistentData().putInt(ExpeditionSessions.PLAYER_RUN_ID,id);
            ExpeditionTimer.clear(member); // Shared session owns the timer.
            member.teleportTo(dungeon,spawn.getX()+0.5,spawn.getY()+1.0,spawn.getZ()+0.5,
                    Set.of(),member.getYRot(),member.getXRot());
            member.sendSystemMessage(Component.literal(
                    "Duskdelvers • Tier "+tier+" • Team expedition #"+id+
                    " • "+members.size()+" adventurers • "+layout.rooms().size()+
                    " rooms • "+(run.seconds()<0?"No time limit":run.timeText()+" time")+
                    " • "+quota.status(member)));
        }
        return true;
    }

    private static void rememberOrigin(ServerPlayer player) {
        // Preserve first external origin when switching between expedition and arrival area.
        if (player.serverLevel().dimension().equals(CHAOS_CAVERNS)) return;
        CompoundTag saved=new CompoundTag();
        saved.putString("dimension",player.serverLevel().dimension().location().toString());
        saved.putDouble("x",player.getX());
        saved.putDouble("y",player.getY());
        saved.putDouble("z",player.getZ());
        saved.putFloat("yaw",player.getYRot());
        saved.putFloat("pitch",player.getXRot());
        player.getPersistentData().put(RETURN_TAG,saved);
    }

    private static int exit(CommandSourceStack source) throws CommandSyntaxException {
        return returnPlayer(source.getPlayerOrException()) ? 1 : 0;
    }

    public static boolean returnPlayer(ServerPlayer player) {
        if (!player.serverLevel().dimension().equals(CHAOS_CAVERNS)) {
            player.sendSystemMessage(Component.literal("You are not inside the Chaos Caverns."));
            return false;
        }
        CompoundTag data=player.getPersistentData().getCompound(RETURN_TAG);
        ServerLevel dest=player.getServer().overworld();
        double x=dest.getSharedSpawnPos().getX()+0.5,y=dest.getSharedSpawnPos().getY()+1,z=dest.getSharedSpawnPos().getZ()+0.5;
        float yaw=player.getYRot(),pitch=player.getXRot();
        if (data.contains("dimension")) {
            try {
                ResourceLocation id=ResourceLocation.parse(data.getString("dimension"));
                ServerLevel saved=player.getServer().getLevel(ResourceKey.create(Registries.DIMENSION,id));
                if (saved!=null) {
                    dest=saved;
                    x=data.getDouble("x"); y=data.getDouble("y"); z=data.getDouble("z");
                    yaw=data.getFloat("yaw"); pitch=data.getFloat("pitch");
                }
            } catch (IllegalArgumentException ignored) {
                // Broken or missing origin dimension: use overworld spawn.
            }
        }
        player.teleportTo(dest,x,y,z,Set.of(),yaw,pitch);
        player.getPersistentData().remove(RETURN_TAG);
        player.getPersistentData().remove(ExpeditionRuntime.ACTIVE);
        player.getPersistentData().remove(ExpeditionSessions.PLAYER_RUN_ID);
        ExpeditionTimer.clear(player);
        player.sendSystemMessage(Component.literal("Returned from the Chaos Caverns."));
        return true;
    }

    private static int teamInvite(CommandSourceStack source,String name) throws CommandSyntaxException {
        ServerPlayer sender=source.getPlayerOrException();
        ServerPlayer target=source.getServer().getPlayerList().getPlayerByName(name);
        if(target==null){
            source.sendFailure(Component.literal("That player is not online."));
            return 0;
        }
        return ExpeditionParty.invite(sender,target)?1:0;
    }
    private static int teamAccept(CommandSourceStack source) throws CommandSyntaxException{
        return ExpeditionParty.accept(source.getPlayerOrException())?1:0;
    }
    private static int teamLeave(CommandSourceStack source) throws CommandSyntaxException{
        return ExpeditionParty.leave(source.getPlayerOrException())?1:0;
    }
    private static int teamReady(CommandSourceStack source) throws CommandSyntaxException{
        ExpeditionParty.toggleFtbReady(source.getPlayerOrException());
        return 1;
    }
    private static int teamStatus(CommandSourceStack source) throws CommandSyntaxException{
        ServerPlayer player=source.getPlayerOrException();
        player.sendSystemMessage(Component.literal(ExpeditionParty.status(player)));
        return 1;
    }
    private static int entries(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player=source.getPlayerOrException();
        player.sendSystemMessage(Component.literal("Duskdelvers • "+ExpeditionQuota.get(player.getServer()).status(player)));
        return 1;
    }

    private static int limits(CommandSourceStack source) {
        StringBuilder result = new StringBuilder("Duskdelvers time limits: ");
        if (!DuskConfig.TIMERS_ENABLED.get()) {
            result.append("disabled");
        } else {
            result.append("outpost=").append(DuskConfig.OUTPOST_MINUTES.get()).append("m");
            for (int i = 0; i < 5; i++) {
                result.append(" | Tier ").append(i + 1).append("=")
                        .append(DuskConfig.TIER_MINUTES.get(i).get()).append("m");
            }
            result.append(" | extraction grace=")
                    .append(DuskConfig.EXTRACTION_GRACE_MINUTES.get()).append("m");
        }
        source.sendSuccess(() -> Component.literal(result.toString()), false);
        return 1;
    }

    private static int time(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        player.sendSystemMessage(Component.literal("Duskdelvers • " + ExpeditionTimer.status(player)));
        return 1;
    }

    private static int status(CommandSourceStack source) throws CommandSyntaxException{
        ServerPlayer player=source.getPlayerOrException();
        var sessions=ExpeditionSessions.get(player.getServer());
        var active=sessions.forPlayer(player);
        if(active!=null){
            player.sendSystemMessage(Component.literal("Expedition #"+active.id()+" • Tier "+
                    active.tier()+" • Rooms cleared "+Integer.bitCount(active.cleared())+"/"+(active.roomCount()-1)+
                    " • Party "+active.members().size()+" • "+
                    (active.completed()?"COMPLETE":active.expired()?"EXPIRED":"active")+
                    " • "+(active.seconds()<0?"Unlimited time":active.timeText()+" remaining")+
                    " • "+ExpeditionQuota.get(player.getServer()).status(player)));
        }else{
            player.sendSystemMessage(Component.literal("Duskdelvers 0.3.0 • No active team expedition • "+
                    ExpeditionTimer.status(player)+" • "+ExpeditionQuota.get(player.getServer()).status(player)));
        }
        return 1;
    }
}
