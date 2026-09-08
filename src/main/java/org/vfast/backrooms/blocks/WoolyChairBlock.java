package org.vfast.backrooms.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import javax.swing.text.html.BlockView;

public class WoolyChairBlock extends HorizontalFacingBlock {
    public static final MapCodec<WoolyChairBlock> CODEC = createCodec(WoolyChairBlock::new);

    public WoolyChairBlock(AbstractBlock.Settings properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<WoolyChairBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(ItemPlacementContext context) {
        return getDefaultState().setValue(FACING, context.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockView level, BlockPos pos, ShapeContext context) {
        Direction currentDirection = state.getValue(FACING);
        return WoolyChairShapes.fromDirection(currentDirection).shape;
    }

    @Override
    protected void createBlockStateDefinition(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    enum WoolyChairShapes {
        NORTH(VoxelShapes.union(
                Block.createCuboidShape(3, 4, 1, 13, 5, 11),
                Block.createCuboidShape(1, 0, 0, 3, 7, 1),
                Block.createCuboidShape(13, 0, 0, 15, 7, 1),
                Block.createCuboidShape(1, 0, 12, 3, 5, 13),
                Block.createCuboidShape(13, 0, 12, 15, 5, 13),
                Block.createCuboidShape(0, 0, 0, 4, 10, 16),
                Block.createCuboidShape(12, 0, 0, 16, 10, 16),
                Block.createCuboidShape(0, 0, 13, 16, 16, 16))),

        SOUTH(VoxelShapes.union(
                Block.createCuboidShape(3, 4, 5, 13, 5, 15),
                Block.createCuboidShape(1, 0, 15, 3, 7, 16),
                Block.createCuboidShape(13, 0, 15, 15, 7, 16),
                Block.createCuboidShape(1, 0, 3, 3, 5, 4),
                Block.createCuboidShape(13, 0, 3, 15, 5, 4),
                Block.createCuboidShape(12, 0, 0, 16, 10, 16),
                Block.createCuboidShape(0, 0, 0, 4, 10, 16),
                Block.createCuboidShape(0, 0, 0, 16, 16, 3))),

        WEST(VoxelShapes.union(
                Block.createCuboidShape(1, 4, 3, 11, 5, 13),
                Block.createCuboidShape(0, 0, 13, 1, 7, 15),
                Block.createCuboidShape(0, 0, 1, 1, 7, 3),
                Block.createCuboidShape(12, 0, 13, 13, 5, 15),
                Block.createCuboidShape(12, 0, 1, 13, 5, 3),
                Block.createCuboidShape(0, 0, 12, 16, 10, 16),
                Block.createCuboidShape(0, 0, 0, 16, 10, 4),
                Block.createCuboidShape(13, 0, 0, 16, 16, 16))),

        EAST(VoxelShapes.union(
                Block.createCuboidShape(5, 4, 3, 15, 5, 13),
                Block.createCuboidShape(15, 0, 1, 16, 7, 3),
                Block.createCuboidShape(15, 0, 13, 16, 7, 15),
                Block.createCuboidShape(3, 0, 1, 4, 5, 3),
                Block.createCuboidShape(3, 0, 13, 4, 5, 15),
                Block.createCuboidShape(0, 0, 0, 16, 10, 4),
                Block.createCuboidShape(0, 0, 12, 16, 10, 16),
                Block.createCuboidShape(0, 0, 0, 3, 16, 16)));

        private final VoxelShape shape;

        WoolyChairShapes(VoxelShape shape) {
            this.shape = shape;
        }

        public static WoolyChairShapes fromDirection(Direction facing) {
            return switch (facing) {
                case SOUTH -> SOUTH;
                case WEST -> WEST;
                case EAST -> EAST;
                default -> NORTH;
            };
        }
    }
}
