package com.townsandkingdoms.duskdelvers;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.server.level.ServerLevel;

/** Right click: start a run/exit. Sneak right click outside: choose difficulty. */
public final class DelversCompass extends Item {
    public DelversCompass(Properties properties) { super(properties); }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!PortalFrames.isActivationFrame(level.getBlockState(context.getClickedPos()).getBlock())) {
            return InteractionResult.PASS;
        }

        if (level instanceof ServerLevel server) {
            boolean activated = PortalFrames.activate(server, context.getClickedPos());
            if (context.getPlayer() != null) {
                context.getPlayer().sendSystemMessage(Component.literal(activated
                        ? "Expedition portal activated! Step into the gateway to begin."
                        : "Incomplete gateway! Use a 4 x 5 frame of one matching tier material, with a clear 2 x 3 opening."));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.serverLevel().dimension().equals(DuskCommands.CHAOS_CAVERNS)) {
                DuskCommands.returnPlayer(serverPlayer);
            } else if (player.isShiftKeyDown()) {
                int tier = serverPlayer.getPersistentData().getInt("DuskdelversCompassTier") % 5 + 1;
                serverPlayer.getPersistentData().putInt("DuskdelversCompassTier", tier);
                serverPlayer.sendSystemMessage(Component.literal("Duskdelvers: Tier " + tier + " selected. Right-click to begin."));
            } else {
                int tier = serverPlayer.getPersistentData().getInt("DuskdelversCompassTier");
                DuskCommands.startPlayer(serverPlayer, tier >= 1 && tier <= 5 ? tier : 1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
