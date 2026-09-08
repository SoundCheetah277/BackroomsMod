package org.vfast.backrooms.blocks.interfaces;


import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SideShapeType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;

public interface CeilingSupportSign {
    default boolean canSurvive(final WorldView level, final BlockPos pos) {
        return level.getBlockState(pos.up()).isSideSolidFullSquare(level, pos.up(), Direction.DOWN, SideShapeType.CENTER);
    }

    default BlockState updateShape(BlockState state, WorldView level, BlockPos pos) {
        return state.canPlaceAt(level, pos) ? state : Blocks.AIR.getDefaultState();
    }
}
