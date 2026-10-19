package com.townsandkingdoms.duskdelvers;

import com.mojang.brigadier.arguments.IntegerArgumentType;
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
                .then(Commands.literal("exit").executes(ctx -> exit(ctx.getSource())))
                .then(Commands.literal("status").executes(ctx -> status(ctx.getSource()))));
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
        if (tier<1 || tier>5) return false;
        ServerLevel dungeon=player.getServer().getLevel(CHAOS_CAVERNS);
        if (dungeon==null) {
            player.sendSystemMessage(Component.literal("Chaos Caverns dimension is missing. Check datapack errors."));
            return false;
        }
        if (!player.serverLevel().dimension().equals(CHAOS_CAVERNS)) rememberOrigin(player);
        int id=ExpeditionIndex.get(player.getServer()).allocate();
        ExpeditionLayout layout=ExpeditionLayout.generate(id,tier,dungeon.getSeed());
        if (layout.rooms().size()<3) {
            player.sendSystemMessage(Component.literal("Failed to generate an expedition route."));
            return false;
        }
        DungeonBuilder.build(dungeon,layout);
        player.getPersistentData().put(ExpeditionRuntime.ACTIVE,layout.toTag());
        var first=layout.rooms().get(0);
        player.teleportTo(dungeon,first.getX()+0.5,first.getY()+1.0,first.getZ()+0.5,
                Set.of(),player.getYRot(),player.getXRot());
        player.sendSystemMessage(Component.literal("Duskdelvers • Tier "+tier+
                " • Expedition #"+id+" • "+layout.rooms().size()+" generated rooms. Defeat the final guardian!"));
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
        player.sendSystemMessage(Component.literal("Returned from the Chaos Caverns."));
        return true;
    }

    private static int status(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player=source.getPlayerOrException();
        CompoundTag tag=player.getPersistentData().getCompound(ExpeditionRuntime.ACTIVE);
        if (tag.contains("rooms")) {
            ExpeditionLayout layout=ExpeditionLayout.fromTag(tag);
            int cleared=Integer.bitCount(tag.getInt("cleared"));
            player.sendSystemMessage(Component.literal("Expedition #"+layout.id()+
                    " • Tier "+layout.tier()+" • Rooms cleared "+cleared+"/"+(layout.rooms().size()-1)+
                    " • "+(tag.getBoolean("completed")?"COMPLETED":"active")+
                    " • Encounter entries "+EncounterProfiles.count()));
        } else {
            player.sendSystemMessage(Component.literal("Duskdelvers 0.2.0: no active expedition. Use /duskdelvers start <1-5>."));
        }
        return 1;
    }
}
