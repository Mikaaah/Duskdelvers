package com.townsandkingdoms.duskdelvers;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Per-player server-authoritative expedition countdown.
 * Counts active in-dimension ticks rather than real-world time, so disconnects,
 * paused integrated servers and other dimensions do not consume expedition time.
 *
 * Stored in player persistent NBT and copied across player clones.
 */
public final class ExpeditionTimer {
    public static final String REMAINING_SECONDS = "DuskdelversTimeSeconds";
    private static final String KIND = "DuskdelversTimerTier";

    private ExpeditionTimer() {}

    public static void start(ServerPlayer player, int tier) {
        var data = player.getPersistentData();
        if (!DuskConfig.TIMERS_ENABLED.get()) {
            clear(player);
            return;
        }
        int minutes = tier >= 1 && tier <= 5
                ? DuskConfig.TIER_MINUTES.get(tier - 1).get()
                : DuskConfig.OUTPOST_MINUTES.get();
        data.putInt(REMAINING_SECONDS, minutes * 60);
        data.putInt(KIND, tier);
        player.sendSystemMessage(Component.literal(
                "Chaos Caverns time limit: " + minutes + " minutes. " +
                        "At 0:00 you will be returned safely with your inventory.").withStyle(ChatFormatting.GOLD));
    }

    public static void clear(ServerPlayer player) {
        var data = player.getPersistentData();
        data.remove(REMAINING_SECONDS);
        data.remove(KIND);
    }

    /** Returns true when the player was extracted and further room ticking should stop. */
    public static boolean tick(ServerPlayer player) {
        if (!player.serverLevel().dimension().equals(DuskCommands.CHAOS_CAVERNS)) return false;

        if (!DuskConfig.TIMERS_ENABLED.get()) {
            clear(player);
            return false;
        }
        var data = player.getPersistentData();
        if (!data.contains(REMAINING_SECONDS, Tag.TAG_INT)) {
            // Covers players who entered via /tp or a portal supplied by another mod.
            var run = data.getCompound(ExpeditionRuntime.ACTIVE);
            start(player, run.contains("tier") ? run.getInt("tier") : 0);
        }

        int seconds = Math.max(0, data.getInt(REMAINING_SECONDS));
        if (seconds == 0) return expire(player);

        seconds -= 1; // Called exactly once per 20 player ticks.
        data.putInt(REMAINING_SECONDS, seconds);

        if (DuskConfig.DISPLAY_TIMER.get()) {
            ChatFormatting color = seconds <= 60 ? ChatFormatting.RED
                    : seconds <= 300 ? ChatFormatting.YELLOW : ChatFormatting.LIGHT_PURPLE;
            player.displayClientMessage(Component.literal(
                    "Duskdelvers • " + format(seconds) + " remaining").withStyle(color), true);
        }

        if (seconds == 300 || seconds == 60 || seconds == 10) {
            player.sendSystemMessage(Component.literal(
                    "Duskdelvers warning: " + format(seconds) + " until automatic extraction.")
                    .withStyle(seconds <= 60 ? ChatFormatting.RED : ChatFormatting.YELLOW));
        }

        if (seconds == 0) return expire(player);
        return false;
    }

    private static boolean expire(ServerPlayer player) {
        player.sendSystemMessage(Component.literal(
                "Time is up! You are being extracted from the Chaos Caverns.")
                .withStyle(ChatFormatting.RED));
        // The normal return routine restores the saved origin and removes the run data.
        // The player retains inventory; there is no item-loss penalty on timeout.
        DuskCommands.returnPlayer(player);
        return true;
    }

    public static void onGuardianDefeated(ServerPlayer player) {
        if (!DuskConfig.TIMERS_ENABLED.get()) return;
        var data = player.getPersistentData();
        if (!data.contains(REMAINING_SECONDS, Tag.TAG_INT)) return;
        int graceSeconds = DuskConfig.EXTRACTION_GRACE_MINUTES.get() * 60;
        int previous = data.getInt(REMAINING_SECONDS);
        if (graceSeconds > previous) {
            data.putInt(REMAINING_SECONDS, graceSeconds);
            player.sendSystemMessage(Component.literal(
                    "Guardian defeated! Extraction window extended to " + format(graceSeconds) + ".")
                    .withStyle(ChatFormatting.GREEN));
        }
    }

    public static String status(ServerPlayer player) {
        if (!DuskConfig.TIMERS_ENABLED.get()) return "Time limit disabled";
        var data = player.getPersistentData();
        if (!player.serverLevel().dimension().equals(DuskCommands.CHAOS_CAVERNS)) {
            return "Timer inactive outside Chaos Caverns";
        }
        if (!data.contains(REMAINING_SECONDS, Tag.TAG_INT)) return "Timer initializing";
        return format(Math.max(0, data.getInt(REMAINING_SECONDS))) + " remaining";
    }

    private static String format(int seconds) {
        return "%d:%02d".formatted(seconds / 60, seconds % 60);
    }
}
