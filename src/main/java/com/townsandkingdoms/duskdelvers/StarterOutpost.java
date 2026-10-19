package com.townsandkingdoms.duskdelvers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.server.level.ServerLevel;

/**
 * Deterministic sample outpost and treasure chamber. This is deliberately NOT
 * advertised as the future procedural-dungeon system.
 *
 * A center-floor marker ensures that entering the dimension again will not
 * rebuild the vault and regenerate its initial contents.
 */
public final class StarterOutpost {
    private static final BlockPos MARKER = new BlockPos(0, 70, 0);
    private StarterOutpost() {}

    public static void ensureCreated(ServerLevel level) {
        if (level.getBlockState(MARKER).is(Blocks.CHISELED_STONE_BRICKS)) {
            return;
        }

        // Load the small region that will be modified before constructing.
        for (int cx = -1; cx <= 4; cx++) {
            for (int cz = -1; cz <= 0; cz++) {
                level.getChunk(cx, cz);
            }
        }

        // Arrival hall: 15 x 15 x 9. Clear the interior even if terrain solid.
        for (int x = -7; x <= 7; x++) {
            for (int z = -7; z <= 7; z++) {
                for (int y = 70; y <= 79; y++) {
                    boolean shell = y == 70 || y == 79 || x == -7 || x == 7 || z == -7 || z == 7;
                    level.setBlock(new BlockPos(x, y, z),
                            (shell ? Blocks.STONE_BRICKS : Blocks.AIR).defaultBlockState(), 3);
                }
            }
        }

        // A safe corridor gives testers a direct path to the first vault.
        for (int x = 7; x <= 43; x++) {
            for (int z = -2; z <= 2; z++) {
                for (int y = 70; y <= 76; y++) {
                    boolean shell = y == 70 || y == 76 || z == -2 || z == 2;
                    level.setBlock(new BlockPos(x, y, z),
                            (shell ? Blocks.COBBLED_DEEPSLATE : Blocks.AIR).defaultBlockState(), 3);
                }
            }
            if (x % 7 == 0) {
                level.setBlock(new BlockPos(x, 71, -1), Blocks.TORCH.defaultBlockState(), 3);
            }
        }

        // Fixed example vault. Future releases will use structure pools and room templates.
        for (int x = 43; x <= 61; x++) {
            for (int z = -8; z <= 8; z++) {
                for (int y = 70; y <= 79; y++) {
                    boolean shell = y == 70 || y == 79 || x == 43 || x == 61 || z == -8 || z == 8;
                    level.setBlock(new BlockPos(x, y, z),
                            (shell ? Blocks.DEEPSLATE_BRICKS : Blocks.AIR).defaultBlockState(), 3);
                }
            }
        }
        // Open an interior doorway at the passage/vault junction.
        for (int y = 71; y <= 74; y++) {
            for (int z = -1; z <= 1; z++) {
                level.setBlock(new BlockPos(43, y, z), Blocks.AIR.defaultBlockState(), 3);
            }
        }
        level.setBlock(new BlockPos(52, 71, -6), Blocks.TORCH.defaultBlockState(), 3);
        level.setBlock(new BlockPos(52, 71, 6), Blocks.TORCH.defaultBlockState(), 3);
        BlockPos lootPos = new BlockPos(53, 71, 0);
        level.setBlock(lootPos, Blocks.CHEST.defaultBlockState(), 3);
        BlockEntity entity = level.getBlockEntity(lootPos);
        if (entity instanceof ChestBlockEntity chest) {
            chest.setItem(0, new ItemStack(Items.IRON_INGOT, 8));
            chest.setItem(1, new ItemStack(Items.GOLD_INGOT, 3));
            chest.setItem(2, new ItemStack(Items.GOLDEN_APPLE, 1));
            chest.setChanged();
        }
        // Commit completion marker last to avoid duplicating rewards after reload.
        level.setBlock(MARKER, Blocks.CHISELED_STONE_BRICKS.defaultBlockState(), 3);
    }
}
