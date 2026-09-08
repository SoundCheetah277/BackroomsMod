package org.vfast.backrooms.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.OrderedTick;
import org.jetbrains.annotations.Nullable;
import org.vfast.backrooms.blocks.entity.TextSignBlockEntity;
import org.vfast.backrooms.blocks.interfaces.CeilingSupportSign;
import org.vfast.backrooms.client.gui.TextSignEditScreen;
import org.vfast.backrooms.interfaces.GuiOpener;

public class TextSignBlock extends BlockWithEntity implements CeilingSupportSign {
    public static final MapCodec<TextSignBlock> CODEC = createCodec(TextSignBlock::new);

    public static final EnumProperty<Direction> ROTATION = Properties.HORIZONTAL_FACING;

    protected TextSignBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<TextSignBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable TextSignBlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new TextSignBlockEntity(worldPosition, blockState);
    }

    @Override
    protected ActionResult useWithoutItem(BlockState state, World level, BlockPos pos, PlayerEntity player, BlockHitResult hitResult) {
        TextSignBlock.TextDirection direction = this.getFacingText(hitResult, state);
        if (player instanceof GuiOpener && direction != TextDirection.NONE && level.getBlockEntity(pos) instanceof TextSignBlockEntity blockEntity) {
            ((GuiOpener) player).openTextSignEdit(blockEntity, pos, this.getFacingText(hitResult, state) == TextDirection.FRONT);
            player.swingHand(Hand.MAIN_HAND);
            return ActionResult.SUCCESS;
        } else {
            return ActionResult.TRY_WITH_EMPTY_HAND;
        }
    }

    private TextSignBlock.TextDirection getFacingText(BlockHitResult hitResult, BlockState state) {
        Direction currentDirection = hitResult.getDirection();
        if (currentDirection == state.getValue(ROTATION)) {
            return TextDirection.FRONT;
        } else if (currentDirection == state.getValue(ROTATION).getOpposite()) {
            return TextDirection.BACK;
        } else {
            return TextDirection.NONE;
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, WorldView level, BlockPos pos) {
        return this.canSurvive(level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, WorldView level, OrderedTick ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, Random random) {
        return this.updateShape(state, level, pos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockView level, BlockPos pos, ShapeContext context) {
        Direction direction = state.getValue(ROTATION);
        return switch (direction) {
            case EAST, WEST -> Block.createCuboidShape(7.75, 0, 0, 8.25, 16, 16);
            default -> Block.createCuboidShape(0, 0, 7.75, 16, 16, 8.25);
        };
    }

    @Override
    public BlockState getStateForPlacement(ItemPlacementContext context) {
        return getDefaultState().setValue(ROTATION, context.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return switch (mirror) {
            case LEFT_RIGHT, FRONT_BACK -> state.setValue(ROTATION, state.getValue(ROTATION).getOpposite());
            default -> super.mirror(state, mirror);
        };
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> state.setValue(ROTATION, state.getValue(ROTATION).getClockWise());
            case COUNTERCLOCKWISE_90 -> state.setValue(ROTATION, state.getValue(ROTATION).getCounterClockWise());
            case CLOCKWISE_180 -> state.setValue(ROTATION, state.getValue(ROTATION).getOpposite());
            default -> super.rotate(state, rotation);
        };
    }

    @Override
    protected void createBlockStateDefinition(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }

    private enum TextDirection {
        FRONT,
        BACK,
        NONE
    }
}
