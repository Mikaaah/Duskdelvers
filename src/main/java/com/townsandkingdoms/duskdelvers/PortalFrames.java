package com.townsandkingdoms.duskdelvers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Player-built 4x5 frame with a 2x3 air-filled opening.
 * The portal can lie in the X or Z plane; frame type determines Tier I-V.
 * Activation uses the craftable Delver's Compass.
 */
public final class PortalFrames {
    private static final Block[] FRAMES = {
            Blocks.CHISELED_DEEPSLATE,       // return gateway, dungeon only
            Blocks.MOSSY_STONE_BRICKS,       // I: Forgotten Depths
            Blocks.PRISMARINE_BRICKS,        // II: Shattered Hollows
            Blocks.AMETHYST_BLOCK,          // III: Resonant Abyss
            Blocks.POLISHED_BLACKSTONE_BRICKS,// IV: Ancient Dominion
            Blocks.CRYING_OBSIDIAN          // V: Chaos Unbound
    };

    private record Shape(BlockPos origin, Direction.Axis axis, int tier) {}

    private PortalFrames() {}

    public static boolean isActivationFrame(Block block) {
        for (int i = 1; i <= 5; i++) if (FRAMES[i] == block) return true;
        return false;
    }

    public static boolean activate(ServerLevel level, BlockPos clicked) {
        if (level.dimension().equals(DuskCommands.CHAOS_CAVERNS)) return false;
        int tier = -1;
        Block frame = level.getBlockState(clicked).getBlock();
        for (int i = 1; i <= 5; i++) if (FRAMES[i] == frame) tier = i;
        if (tier < 1) return false;

        // The player may right-click any of the 14 frame blocks.
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            int horizontal = axis == Direction.Axis.X ? clicked.getX() : clicked.getZ();
            for (int start = horizontal-3; start <= horizontal; start++) {
                for (int bottom = clicked.getY()-4; bottom <= clicked.getY(); bottom++) {
                    BlockPos origin = axis == Direction.Axis.X
                            ? new BlockPos(start, bottom, clicked.getZ())
                            : new BlockPos(clicked.getX(), bottom, start);
                    if (hasFrame(level, origin, axis, tier) && interiorAir(level, origin, axis)) {
                        fillPortal(level, origin, axis, tier);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isComplete(BlockGetter world, BlockPos inside, Direction.Axis axis, int tier) {
        if (tier < 0 || tier > 5) return false;
        int horizontal = axis == Direction.Axis.X ? inside.getX() : inside.getZ();
        // 'inside' must lie in one of the six 2x3 interior cells.
        for (int start = horizontal-2; start <= horizontal-1; start++) {
            for (int bottom = inside.getY()-3; bottom <= inside.getY()-1; bottom++) {
                BlockPos origin = axis == Direction.Axis.X
                        ? new BlockPos(start, bottom, inside.getZ())
                        : new BlockPos(inside.getX(), bottom, start);
                if (hasFrame(world, origin, axis, tier) && interiorPortal(world, origin, axis, tier)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static BlockPos cell(BlockPos origin, Direction.Axis axis, int horizontal, int vertical) {
        return axis == Direction.Axis.X
                ? origin.offset(horizontal, vertical, 0)
                : origin.offset(0, vertical, horizontal);
    }

    private static boolean hasFrame(BlockGetter level, BlockPos origin, Direction.Axis axis, int tier) {
        for (int x = 0; x < 4; x++) for (int y = 0; y < 5; y++) {
            if (x != 0 && x != 3 && y != 0 && y != 4) continue;
            if (!level.getBlockState(cell(origin, axis, x, y)).is(FRAMES[tier])) return false;
        }
        return true;
    }

    private static boolean interiorAir(BlockGetter level, BlockPos origin, Direction.Axis axis) {
        for (int x = 1; x <= 2; x++) for (int y = 1; y <= 3; y++) {
            if (!level.getBlockState(cell(origin, axis, x, y)).isAir()) return false;
        }
        return true;
    }

    private static boolean interiorPortal(BlockGetter level, BlockPos origin,
                                          Direction.Axis axis, int tier) {
        GatewayBlock block = Duskdelvers.gatewayForTier(tier);
        for (int x = 1; x <= 2; x++) for (int y = 1; y <= 3; y++) {
            BlockState state = level.getBlockState(cell(origin, axis, x, y));
            if (!state.is(block) || state.getValue(GatewayBlock.AXIS) != axis) return false;
        }
        return true;
    }

    private static void fillPortal(Level level, BlockPos origin, Direction.Axis axis, int tier) {
        BlockState state = Duskdelvers.gatewayForTier(tier).defaultBlockState()
                .setValue(GatewayBlock.AXIS, axis);
        // Flag 2 prevents neighbor updates from prematurely invalidating partially filled portals.
        for (int x = 1; x <= 2; x++) for (int y = 1; y <= 3; y++) {
            level.setBlock(cell(origin, axis, x, y), state, 2);
        }
    }

    /**
     * Built-in return frame at the dungeon's entrance and after guardian defeat.
     * Kept away from the central spawn point and the usual corridor entrances.
     */
    public static void placeReturnGateway(ServerLevel world, BlockPos roomCenter) {
        BlockPos origin = roomCenter.offset(-1, 0, -6);
        for (int x = 0; x < 4; x++) for (int y = 0; y < 5; y++) {
            if (x != 0 && x != 3 && y != 0 && y != 4) continue;
            world.setBlock(cell(origin, Direction.Axis.X, x, y), FRAMES[0].defaultBlockState(), 2);
        }
        fillPortal(world, origin, Direction.Axis.X, 0);
    }

    public static String materialName(int tier) {
        return switch (tier) {
            case 1 -> "Mossy Stone Bricks";
            case 2 -> "Prismarine Bricks";
            case 3 -> "Block of Amethyst";
            case 4 -> "Polished Blackstone Bricks";
            case 5 -> "Crying Obsidian";
            default -> "Unknown";
        };
    }
}
