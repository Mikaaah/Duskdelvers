package com.townsandkingdoms.duskdelvers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;

/**
 * 0 = expedition return gateway; 1..5 = player-buildable tier gateway.
 * Only players trigger dimension travel, and all effects run server-side.
 */
public final class GatewayBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    private static final String NEXT_USE_TICK = "DuskdelversGatewayCooldown";
    private final int tier;

    public GatewayBlock(int tier, Properties properties) {
        super(properties);
        this.tier = tier;
        registerDefaultState(stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    public int tier() {
        return tier;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(level instanceof ServerLevel world) || !(entity instanceof ServerPlayer player)) return;
        long now = player.getServer().getTickCount();
        if (now < player.getPersistentData().getLong(NEXT_USE_TICK)) return;

        if (!PortalFrames.isComplete(world, pos, state.getValue(AXIS), tier)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            return;
        }

        if (tier == 0) {
            if (!world.dimension().equals(DuskCommands.CHAOS_CAVERNS)) return;
            player.getPersistentData().putLong(NEXT_USE_TICK, now + 100);
            if (!DuskCommands.returnPlayer(player)) {
                player.getPersistentData().remove(NEXT_USE_TICK);
            }
        } else {
            if (world.dimension().equals(DuskCommands.CHAOS_CAVERNS)) return;

            double oldX = player.getX(), oldY = player.getY(), oldZ = player.getZ();
            Vec3 velocity = player.getDeltaMovement();
            player.getPersistentData().putLong(NEXT_USE_TICK, now + 100);
            if (DuskCommands.startPlayer(player, tier)) {
                // Do not return into an active portal and instantly travel again.
                DuskCommands.adjustPortalReturn(player, world, pos, state.getValue(AXIS),
                        oldX, oldY, oldZ, velocity);
            } else {
                player.getPersistentData().remove(NEXT_USE_TICK);
            }
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos,
                                   Block neighbor, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighbor, neighborPos, movedByPiston);
        if (!level.isClientSide && !PortalFrames.isComplete(level, pos, state.getValue(AXIS), tier)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }
    }
}
