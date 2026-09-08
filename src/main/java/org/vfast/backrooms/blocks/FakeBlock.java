package org.vfast.backrooms.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.vfast.backrooms.interfaces.LevelPortal;
import org.vfast.backrooms.world.BackroomsLevels;

import java.util.Set;

public class FakeBlock extends Block implements LevelPortal {
    public static final MapCodec<FakeBlock> CODEC = createCodec(FakeBlock::new);

    public static final EnumProperty<Mimic> MIMIC = EnumProperty.of("mimic_block", FakeBlock.Mimic.class);

    public FakeBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(MIMIC, Mimic.GRASS));
    }

    @Override
    public void entityInside(BlockState state, World level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        LevelPortal.super.entityInside(state, level, pos, entity, EntityBounding.HEAD);

        Random random = level.getRandom();
        entity.slowMovement(state, new Vec3d(0.9F, 1.5, 0.9F));
        BlockStateParticleEffect particle = new BlockStateParticleEffect(ParticleTypes.BLOCK, state);
        level.addImportantParticle(
                particle,
                entity.getX() + MathHelper.nextBetween(random, -1.0F, 1.0F) * 0.3f,
                pos.getY() + 1,
                entity.getZ()  + MathHelper.nextBetween(random, -1.0F, 1.0F) * 0.3f,
                0.0f,
                0.0f,
                0.0f
        );
    }

    @Override
    public @Nullable TeleportTransition getPortalDestination(ServerWorld currentLevel, Entity entity, BlockPos portalEntryPos) {
        assert entity.isAlive();
        this.prepareEntity(entity, true);

        RegistryKey<World> dimension = BackroomsLevels.LEVEL_0;
        ServerWorld newLevel = currentLevel.getServer().getWorld(dimension);

        if (newLevel != null && newLevel != currentLevel) {
            LevelPortal.SpawnLocation spawnLoc = this.selectStartPosition(portalEntryPos, newLevel, null);

            if (entity instanceof ServerPlayerEntity) {
                ServerPlayerEntity.RespawnConfig respawn = LevelPortal.getSpawnConfig(spawnLoc.position(), spawnLoc.yRot(), spawnLoc.xRot(), dimension);
                ((ServerPlayerEntity) entity).setRespawnPosition(respawn, false);
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

    private void prepareEntity(Entity entity, boolean ongoing) {
        if (entity instanceof LivingEntity) {
            LevelPortal.prepareEntity((LivingEntity) entity, ongoing);
        }
    }

    @Override
    public BlockState getStateForPlacement(ItemPlacementContext context) {
        return getDefaultState().setValue(MIMIC, Mimic.GRASS);
    }
    @Override
    protected void createBlockStateDefinition(StateManager.Builder<Block, BlockState> builder) {
        builder.add(MIMIC);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView level, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    protected BlockSoundGroup getSoundType(BlockState state) {
        return state.getValue(MIMIC).getSoundType();
    }

    @Override
    public Block invalidBlock() {
        return BackroomsBlocks.MOIST_SILK;
    }

    @Override
    public BlockState suffocatingBlock(BlockState currentState) {
        return currentState.getValue(MIMIC).getBlock().defaultBlockState();
    }

    @Override
    protected boolean isPathfindable(BlockState state, NavigationType type) {
        return true;
    }

    enum Mimic implements StringIdentifiable {
        GRASS,
        SAND;

        @Override
        public String getSerializedName() {
            return switch (this) {
                case GRASS -> "grass_block";
                case SAND -> "sand";
            };
        }

        public BlockSoundGroup getSoundType() {
            return switch (this) {
                case GRASS -> BlockSoundGroup.GRASS;
                case SAND -> BlockSoundGroup.SAND;
            };
        }

        public Block getBlock() {
            return switch (this) {
                case GRASS -> Blocks.GRASS_BLOCK;
                case SAND -> Blocks.SAND;
            };
        }
    }
}
