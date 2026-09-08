package org.vfast.backrooms.interfaces;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Noclippable {
    int NOCLIP_TICKS = 3600;
    int DEFAULT_RADIUS = 15;
    List<Block> SURFACE_BLOCKS = List.of(Blocks.STONE, Blocks.ANDESITE, Blocks.GRANITE, Blocks.DIORITE, Blocks.DIRT);
    List<Block> VALID_BLOCKS = List.of(Blocks.SAND, Blocks.GRASS_BLOCK);

    default @Nullable BlockPos lookAround(World level, BlockPos center, int radius) {
        int x = center.getX();
        int y = center.getY();
        int z = center.getZ();

        Random random = level.getRandom();
        @Nullable BlockPos finalPos = null;

        for (int xx = x; finalPos == null; xx = x + MathHelper.floor(MathHelper.nextBetween(random, -radius, radius))) {
            for (int zz = z; finalPos == null; zz = z + MathHelper.floor(MathHelper.nextBetween(random, -radius, radius))) {
                BlockPos lookingPos = new BlockPos(xx, y, zz);
                BlockState lookingState = level.getBlockState(lookingPos);
                Block lookingBlock = lookingState.getBlock();

                if (Noclippable.VALID_BLOCKS.contains(lookingBlock) && level.isInBuildLimit(lookingPos)) {
                    finalPos = lookingPos;
                } else if (Noclippable.SURFACE_BLOCKS.contains(lookingBlock)) {
                    y++;
                    continue;
                } else if (lookingBlock == Blocks.AIR || lookingBlock == Blocks.CAVE_AIR) {
                    y--;
                    continue;
                }
            }
        }

        return finalPos;
    }

    default @Nullable BlockPos lookAround(World level, BlockPos center) {
        return this.lookAround(level, center, Noclippable.DEFAULT_RADIUS);
    }
}
