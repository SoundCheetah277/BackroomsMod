package org.vfast.backrooms.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;

import javax.swing.text.html.BlockView;

public class VentBlock extends WallMountedBlock {
    public static final MapCodec<VentBlock> CODEC = createCodec(VentBlock::new);

    private static final VoxelShape[] FLOOR_SHAPES = new VoxelShape[] {
        Block.createCuboidShape(1, 0, 4.5, 15, 0.25, 11.5), // s
        Block.createCuboidShape(4.5, 0, 1, 11.5, 0.25, 15), // w
        Block.createCuboidShape(1, 0, 4.5, 15, 0.25, 11.5), // n
        Block.createCuboidShape(4.5, 0, 1, 11.5, 0.25, 15)  // e
    };

    private static final VoxelShape[] WALL_SHAPES = new VoxelShape[] {
        Block.createCuboidShape(1, 4.5, 0, 15, 11.5, 0.25),
        Block.createCuboidShape(15.75, 4.5, 1, 16, 11.5, 15),
        Block.createCuboidShape(1, 4.5, 15.75, 15, 11.5, 16),
        Block.createCuboidShape(0, 4.5, 1, 0.25, 11.5, 15)
    };

    private static final VoxelShape[] CEILING_SHAPES = new VoxelShape[] {
        Block.createCuboidShape(1, 15.75, 4.5, 15, 16, 11.5),
        Block.createCuboidShape(4.5, 15.75, 1, 11.5, 16, 15),
        Block.createCuboidShape(1, 15.75, 4.5, 15, 16, 11.5),
        Block.createCuboidShape(4.5, 15.75, 1, 11.5, 16, 15)
    };

    public VentBlock(AbstractBlock.Settings properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FACE, AttachFace.WALL));
    }

    @Override
    protected MapCodec<? extends WallMountedBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView level, BlockPos pos, ShapeContext context) {
        if (state.getValue(FACE) == AttachFace.FLOOR) {
            return FLOOR_SHAPES[horizontalIndex(state.getValue(FACING))];
        } else if (state.getValue(FACE) == AttachFace.CEILING) {
            return CEILING_SHAPES[horizontalIndex(state.getValue(FACING))];
        }
        return WALL_SHAPES[horizontalIndex(state.getValue(FACING))];
    }

    @Override
    protected void createBlockStateDefinition(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE);
    }

    private static int horizontalIndex(Direction direction) {
        return switch (direction) {
            case SOUTH -> 0;
            case WEST -> 1;
            case NORTH -> 2;
            default -> 3;
        };
    }
}
