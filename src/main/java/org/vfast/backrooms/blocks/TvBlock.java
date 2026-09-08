package org.vfast.backrooms.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.Orientation;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.vfast.backrooms.BackroomsMod;
import org.vfast.backrooms.blocks.entity.TvBlockEntity;
import org.vfast.backrooms.sounds.BackroomsSounds;

public class TvBlock extends BlockWithEntity {
    public static final MapCodec<TvBlock> CODEC = createCodec(TvBlock::new);

    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BooleanProperty.of("powered");

    public static final int SOUND_DURATION = 112; // in ticks

    protected TvBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    protected void tick(BlockState state, ServerWorld level, BlockPos pos, Random random) {
        if (state.getValue(POWERED) && !level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.cycle(POWERED), 2);
        }
    }

    @Override
    public void animateTick(BlockState state, World level, BlockPos pos, Random random) {
        int lastPoweredTick;
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TvBlockEntity tv) {
            lastPoweredTick = tv.getLastPoweredTick();
        } else { return; }

        boolean hasPower = level.hasNeighborSignal(pos);
        level.setBlock(pos, state.setValue(POWERED, hasPower), 2);

        if (hasPower) {
            if (lastPoweredTick <= 0) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, BackroomsSounds.TV_SONG.value(), SoundCategory.BLOCKS, 0.5f, 1.0f, false);
            }
            tv.addPoweredTick();
        }

        if (tv.isComplete()) {
            tv.resetPoweredTick();
        }
    }

    @Override
    protected void neighborChanged(BlockState state, World level, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
        if (!level.isClient()) {
            boolean isPowered = state.getValue(POWERED);
            if (isPowered) {
                level.scheduleTick(pos, this, 4);
            }
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(ItemPlacementContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(POWERED, context.getWorld().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockView level, BlockPos pos, ShapeContext context) {
        return Block.createCuboidShape(1, 0, 1, 15, 13, 15);
    }

    @Override
    protected void createBlockStateDefinition(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    protected MapCodec<TvBlock> codec() {
        return CODEC;
    }

    protected BlockState rotate(final BlockState state, final BlockRotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    protected BlockState mirror(final BlockState state, final BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new TvBlockEntity(worldPosition, blockState);
    }
}
