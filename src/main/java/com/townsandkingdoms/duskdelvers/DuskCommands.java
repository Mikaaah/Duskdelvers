package com.townsandkingdoms.duskdelvers;

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

/** Temporary command-driven travel interface for the 0.1.0 foundation. */
public final class DuskCommands {
    public static final ResourceKey<Level> CHAOS_CAVERNS = ResourceKey.create(
            Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath("duskdelvers", "chaos_caverns"));
    private static final String RETURN_TAG = "DuskdelversReturn";

    private DuskCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("duskdelvers")
                .then(Commands.literal("enter").executes(ctx -> enter(ctx.getSource())))
                .then(Commands.literal("exit").executes(ctx -> exit(ctx.getSource())))
                .then(Commands.literal("status").executes(ctx -> status(ctx.getSource()))));
    }

    private static int enter(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (player.level().dimension().equals(CHAOS_CAVERNS)) {
            source.sendFailure(Component.literal("You are already in the Chaos Caverns."));
            return 0;
        }
        ServerLevel caverns = source.getServer().getLevel(CHAOS_CAVERNS);
        if (caverns == null) {
            source.sendFailure(Component.literal("Chaos Caverns dimension is unavailable. Check the server's datapack errors."));
            return 0;
        }

        // Remember the origin per player so exiting restores their previous dimension and position.
        CompoundTag origin = new CompoundTag();
        origin.putString("dimension", player.level().dimension().location().toString());
        origin.putDouble("x", player.getX());
        origin.putDouble("y", player.getY());
        origin.putDouble("z", player.getZ());
        origin.putFloat("yaw", player.getYRot());
        origin.putFloat("pitch", player.getXRot());
        player.getPersistentData().put(RETURN_TAG, origin);

        StarterOutpost.ensureCreated(caverns);
        player.teleportTo(caverns, 0.5D, 72.0D, 0.5D, Set.of(), player.getYRot(), player.getXRot());
        source.sendSuccess(() -> Component.literal("Entered the Chaos Caverns. Follow the stone-brick passage to the starter vault. Use /duskdelvers exit to return."), false);
        return 1;
    }

    private static int exit(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!player.level().dimension().equals(CHAOS_CAVERNS)) {
            source.sendFailure(Component.literal("You are not in the Chaos Caverns."));
            return 0;
        }

        CompoundTag data = player.getPersistentData().getCompound(RETURN_TAG);
        ServerLevel destination = source.getServer().overworld();
        double x = destination.getSharedSpawnPos().getX() + 0.5D;
        double y = destination.getSharedSpawnPos().getY() + 1D;
        double z = destination.getSharedSpawnPos().getZ() + 0.5D;
        float yaw = player.getYRot();
        float pitch = player.getXRot();

        if (data.contains("dimension")) {
            try {
                ResourceLocation id = ResourceLocation.parse(data.getString("dimension"));
                ServerLevel savedLevel = source.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, id));
                if (savedLevel != null) {
                    destination = savedLevel;
                    x = data.getDouble("x");
                    y = data.getDouble("y");
                    z = data.getDouble("z");
                    yaw = data.getFloat("yaw");
                    pitch = data.getFloat("pitch");
                }
            } catch (IllegalArgumentException ignored) {
                // Corrupt/removed dimension: return to the overworld spawn safely.
            }
        }

        player.teleportTo(destination, x, y, z, Set.of(), yaw, pitch);
        player.getPersistentData().remove(RETURN_TAG);
        source.sendSuccess(() -> Component.literal("You have returned from the Chaos Caverns."), false);
        return 1;
    }

    private static int status(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal("Duskdelvers 0.1.0-alpha: cave dimension, starter outpost and vault. Procedural dungeons, tiers, bosses and optional mod adapters are not yet enabled."), false);
        return 1;
    }
}
