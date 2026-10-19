package com.townsandkingdoms.duskdelvers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import java.util.List;
import java.util.Random;

/** Builds a connected, varied dungeon on demand. No forced chunks are retained. */
public final class DungeonBuilder {
    private static final int Y = ExpeditionLayout.FLOOR_Y;
    private DungeonBuilder() {}

    public static void build(ServerLevel level, ExpeditionLayout layout) {
        List<BlockPos> rooms = layout.rooms();
        Random random = new Random(level.getSeed() ^ (layout.id() * 92837111L));
        for (int i = 0; i < rooms.size(); i++) {
            BlockPos center = rooms.get(i);
            // Loading is done before any setBlock to avoid late chunk writes.
            for (int dx=-1;dx<=1;dx++)
                for (int dz=-1;dz<=1;dz++)
                    level.getChunk((center.getX() >> 4)+dx, (center.getZ() >> 4)+dz);
            buildRoom(level, center, layout.tier(), i, i == rooms.size()-1, random);
        }
        for (int i = 1; i < rooms.size(); i++) {
            connect(level,rooms.get(i-1),rooms.get(i));
        }
        for (int i = 1; i < rooms.size()-1; i++) {
            if (i % 2 == 1) {
                placeChest(level,rooms.get(i).offset(4,1,4),layout.tier(),random,false);
            }
        }
        // The permanent entrance gateway lets players leave without their compass.
        PortalFrames.placeReturnGateway(level, rooms.get(0));
        // Finish with an arrival marker. This marker does not regenerate rewards.
        level.setBlock(rooms.get(0).below(),Blocks.CHISELED_STONE_BRICKS.defaultBlockState(),2);
    }

    private static Block wallBlock(int tier) {
        return switch (tier) {
            case 1 -> Blocks.STONE_BRICKS;
            case 2 -> Blocks.MOSSY_STONE_BRICKS;
            case 3 -> Blocks.DEEPSLATE_BRICKS;
            case 4 -> Blocks.POLISHED_BLACKSTONE_BRICKS;
            default -> Blocks.CRYING_OBSIDIAN;
        };
    }

    private static void buildRoom(ServerLevel level, BlockPos center, int tier,
                                  int index, boolean boss, Random random) {
        Block masonry=wallBlock(tier);
        for (int x=-8;x<=8;x++) {
            for (int z=-8;z<=8;z++) {
                for (int y=-1;y<=8;y++) {
                    boolean shell=y==-1||y==8||Math.abs(x)==8||Math.abs(z)==8;
                    Block material = shell ? masonry : Blocks.AIR;
                    if (y==-1 && Math.abs(x)<=1 && Math.abs(z)<=1)
                        material=Blocks.POLISHED_ANDESITE;
                    level.setBlock(center.offset(x,y,z),material.defaultBlockState(),2);
                }
            }
        }
        // Lit corners to ensure readability without daylight.
        for (int x:new int[]{-6,6}) {
            for (int z:new int[]{-6,6}) {
                level.setBlock(center.offset(x,0,z),Blocks.POLISHED_DEEPSLATE.defaultBlockState(),2);
                level.setBlock(center.offset(x,1,z),Blocks.SEA_LANTERN.defaultBlockState(),2);
            }
        }
        if (boss) {
            // Boss rooms are visibly different from ordinary encounters.
            for (int x=-5;x<=5;x++)
                for (int z=-5;z<=5;z++)
                    if ((x*x+z*z)>=15 && (x*x+z*z)<=28)
                        level.setBlock(center.offset(x,-1,z),Blocks.GILDED_BLACKSTONE.defaultBlockState(),2);
            level.setBlock(center.offset(0,-1,0),Blocks.CHISELED_DEEPSLATE.defaultBlockState(),2);
        } else if (index>0) {
            for (int j=0;j<3;j++) {
                int x=random.nextBoolean() ? -5 : 5;
                int z=-4+random.nextInt(9);
                if (Math.abs(z)<3) z=4;
                level.setBlock(center.offset(x,-1,z),Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),2);
            }
        }
    }

    private static void connect(ServerLevel level,BlockPos a,BlockPos b) {
        if (a.getX()!=b.getX()) {
            int first=Math.min(a.getX(),b.getX())+8;
            int last=Math.max(a.getX(),b.getX())-8;
            for (int x=first;x<=last;x++)
                for (int z=-2;z<=2;z++)
                    for (int y=-1;y<=5;y++) {
                        boolean wall=y==-1||y==5||Math.abs(z)==2;
                        level.setBlock(new BlockPos(x,Y+y,a.getZ()),
                                (wall?Blocks.COBBLED_DEEPSLATE:Blocks.AIR).defaultBlockState(),2);
                    }
            openDoor(level,new BlockPos(a.getX()+(b.getX()>a.getX()?8:-8),Y,a.getZ()),true);
            openDoor(level,new BlockPos(b.getX()+(a.getX()>b.getX()?8:-8),Y,b.getZ()),true);
        } else {
            int first=Math.min(a.getZ(),b.getZ())+8;
            int last=Math.max(a.getZ(),b.getZ())-8;
            for (int z=first;z<=last;z++)
                for (int x=-2;x<=2;x++)
                    for (int y=-1;y<=5;y++) {
                        boolean wall=y==-1||y==5||Math.abs(x)==2;
                        level.setBlock(new BlockPos(a.getX()+x,Y+y,z),
                                (wall?Blocks.COBBLED_DEEPSLATE:Blocks.AIR).defaultBlockState(),2);
                    }
            openDoor(level,new BlockPos(a.getX(),Y,a.getZ()+(b.getZ()>a.getZ()?8:-8)),false);
            openDoor(level,new BlockPos(b.getX(),Y,b.getZ()+(a.getZ()>b.getZ()?8:-8)),false);
        }
    }

    private static void openDoor(ServerLevel level,BlockPos center,boolean eastWest) {
        for (int horizontal=-1;horizontal<=1;horizontal++) {
            for (int dy=0;dy<=4;dy++) {
                BlockPos pos = eastWest ? center.offset(0,dy,horizontal)
                        : center.offset(horizontal,dy,0);
                level.setBlock(pos,Blocks.AIR.defaultBlockState(),2);
            }
        }
    }

    public static void bossReward(ServerLevel level, BlockPos boss, int tier, int run) {
        // Check stable marker before placing to prevent duplicate payout.
        BlockPos chestPos = boss.offset(4,1,4);
        if (level.getBlockState(chestPos).is(Blocks.CHEST)) return;
        placeChest(level,chestPos,tier,new Random(level.getSeed() ^ run ^ 0xD051L),true);
        level.setBlock(boss.offset(4,-1,4),Blocks.EMERALD_BLOCK.defaultBlockState(),2);
    }

    private static void placeChest(ServerLevel level,BlockPos pos,int tier,Random random,boolean boss) {
        level.setBlock(pos,Blocks.CHEST.defaultBlockState(),3);
        BlockEntity blockEntity=level.getBlockEntity(pos);
        if (!(blockEntity instanceof ChestBlockEntity chest)) return;
        chest.setItem(0,new ItemStack(Items.IRON_INGOT, 2+tier+random.nextInt(4)));
        chest.setItem(1,new ItemStack(Items.GOLD_INGOT, tier+random.nextInt(3)));
        chest.setItem(2,new ItemStack(Items.EMERALD, 1+random.nextInt(tier+1)));
        if (tier>=2) chest.setItem(3,new ItemStack(Items.DIAMOND, 1+random.nextInt(Math.max(1,tier-1))));
        if (tier>=3) chest.setItem(4,new ItemStack(Items.EXPERIENCE_BOTTLE, 2+tier));
        if (tier>=4) chest.setItem(5,new ItemStack(Items.ECHO_SHARD,1+random.nextInt(2)));
        if (tier>=5 && boss) chest.setItem(6,new ItemStack(Items.NETHERITE_SCRAP,1));
        if (boss) chest.setItem(8,new ItemStack(Items.GOLDEN_APPLE,Math.min(3,tier)));
        chest.setChanged();
    }
}
