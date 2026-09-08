package org.vfast.backrooms.blocks;

import com.llamalad7.mixinextras.lib.antlr.runtime.atn.Transition;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.vfast.backrooms.attachments.BackroomsAttachments;
import org.vfast.backrooms.interfaces.LevelPortal;
import org.vfast.backrooms.world.BackroomsLevels;

import java.util.Set;

public class BackroomsPortalBlock extends HorizontalFacingBlock implements LevelPortal {
    public static final MapCodec<BackroomsPortalBlock> CODEC = createCodec(BackroomsPortalBlock::new);

    public static final BooleanProperty OVERWORLD = BooleanProperty.create("overworld");

    public BackroomsPortalBlock(AbstractBlock.Settings properties) {
        super(properties);
        setDefaultState(StateManager.any().setValue(FACING, Direction.NORTH).setValue(OVERWORLD, true));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> codec() {
        return CODEC;
    }

    @Override
    public void entityInside(BlockState state, World level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        LevelPortal.super.entityInside(state, level, pos, entity);
    }

    @Override
    protected ActionResult useWithoutItem(BlockState state, World level, BlockPos pos, PlayerEntity player, BlockHitResult hitResult) {
        if (player.gameMode() == GameMode.CREATIVE) {
            BlockState newState = state.cycle(OVERWORLD);
            level.setBlock(pos, newState, 3);
            Hand hand = player.preferredHand != null ? player.preferredHand : Hand.MAIN_HAND;
            player.swingHand(hand);
            return ActionResult.SUCCESS;
        } else {
            return super.onUse(state, level, pos, player, hitResult);
        }
    }

    @Override
    public BlockState getStateForPlacement(ItemPlacementContext context) {
        return getDefaultState().setValue(FACING, context.getHorizontalPlayerFacing().getOpposite()).setValue(OVERWORLD, true);
    }
    @Override
    protected void createBlockStateDefinition(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, OVERWORLD);
    }

    @Override
    public int getPortalTransitionTime(ServerWorld level, Entity entity) {
        return 0;
    }

    @Override
    public @Nullable TeleportTransition getPortalDestination(ServerWorld currentLevel, Entity entity, BlockPos portalEntryPos) {
        assert entity.isAlive();
        this.prepareEntity(entity, true);

        boolean fromLevel = BackroomsLevels.isBackrooms(currentLevel.dimension());
        RegistryKey<World> newDimension = fromLevel ? World.OVERWORLD : BackroomsLevels.LEVEL_0;
        ServerWorld newLevel = currentLevel.getServer().getWorld(newDimension);
        if (newLevel != null) {
            LevelPortal.SpawnLocation spawnLoc;
            if (!fromLevel) {
                // going to level 0
                spawnLoc = this.selectStartPosition(portalEntryPos, newLevel, BackroomsBlocks.BACKROOMS_PORTAL.getDefaultState().setValue(OVERWORLD, false));
            } else {
                // going back to overworld
                BlockPos newPos = newLevel.getRespawnData().pos();
                float pitch = newLevel.getRespawnData().pitch();
                float yaw = newLevel.getRespawnData().yaw();

                BlockPos respawn = entity.getAttached(BackroomsAttachments.SAVED_SPAWN);
                if (respawn != null) {
                    newPos = respawn;
                }

                spawnLoc = new LevelPortal.SpawnLocation(newPos, pitch, yaw);
            }

            this.prepareEntity(entity, false);

            BlockPos blockPos = spawnLoc.position();
            double x = blockPos.getX() + 0.5d;
            double y = blockPos.getY();
            double z = blockPos.getZ() + 0.5d;
            Vec3d pos = new Vec3d(x, y, z);

            return new TeleportTransition(newLevel, pos, Vec3d.ZERO, spawnLoc.yRot(), spawnLoc.xRot(), Set.of(), LevelPortal::affectPlayer);
        } else {
            this.prepareEntity(entity, false);
            return null;
        }
    }

    @Override
    public BlockState suffocatingBlock(BlockState blockState) {
        boolean isConcrete = blockState.getValue(OVERWORLD);
        if (isConcrete) {
            return BackroomsBlocks.STAINED_CONCRETE.getDefaultState();
        } else {
            return BackroomsBlocks.MOIST_SILK.getDefaultState();
        }
    }

    private void prepareEntity(Entity entity, boolean ongoing) {
        if (entity instanceof LivingEntity) {
            LevelPortal.prepareEntity((LivingEntity) entity, ongoing);
        }
    }

    @Override
    public Block invalidBlock() {
        return BackroomsBlocks.MOIST_SILK;
    }

    @Override
    public Transition getLocalTransition() {
        return Transition.NONE;
    }
}
