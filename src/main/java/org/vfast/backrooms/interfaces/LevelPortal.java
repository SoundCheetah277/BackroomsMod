package org.vfast.backrooms.interfaces;

import com.llamalad7.mixinextras.lib.antlr.runtime.atn.Transition;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.Portal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import org.jetbrains.annotations.Nullable;
import org.vfast.backrooms.BackroomsMod;
import org.vfast.backrooms.attachments.BackroomsAttachments;
import org.vfast.backrooms.attachments.PlayerSnapshot;
import org.vfast.backrooms.blocks.BackroomsPortalBlock;
import org.vfast.backrooms.sounds.BackroomsSounds;
import org.vfast.backrooms.world.BackroomsGameRules;
import org.vfast.backrooms.world.BackroomsLevels;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Use this when a block is used to **travel to a Backrooms level** */
public interface LevelPortal extends Portal {
    Map<LivingEntity, Float> transferEntitySpeed = new HashMap<>();

    default void entityInside(BlockState state, World level, BlockPos pos, Entity entity, EntityBounding entityBounding) {
        boolean suffocating = this.shouldSuffocate(entity, pos);
        if (suffocating && level.isClient()) {
            this.simulateSuffocation(this.suffocatingBlock(state));
        } else if (!suffocating && level.isClient()) {
            this.simulateSuffocation(null);
        }

        boolean isPlayerInside = (entity instanceof PlayerEntity && this.fullyInBounding(entity, pos, entityBounding));
        boolean isEntityInside = (!(entity instanceof PlayerEntity) && this.lazyBounding(entity, pos));

        if (entity.canUsePortals(false) && !entity.hasPortalCooldown() && (isPlayerInside || isEntityInside)) {
            entity.tryUsePortal(this, pos);

            long seed = Random.create().nextLong();
            if (!level.isClient()) {
                boolean isSmallEntity = entity instanceof ItemEntity || entity instanceof ProjectileEntity;
                if (!isSmallEntity) {
                    boolean isHostile = entity instanceof Monster;
                    SoundCategory entitySource = isHostile ? SoundCategory.HOSTILE : SoundCategory.NEUTRAL;

                    level.playSound(entity, entity.getX(), entity.getY(), entity.getZ(), BackroomsSounds.NOCLIP, isPlayerInside ? SoundCategory.PLAYERS : entitySource, 1.0f, 1.0f, seed);
                } else {
                    level.playSound(entity, entity.getX(), entity.getY(), entity.getZ(), BackroomsSounds.NOCLIP_SMALL, SoundCategory.AMBIENT, 1.0f, 1.0f, seed);
                }
            }
        }
    }

    default void entityInside(BlockState state, World level, BlockPos pos, Entity entity) {
        this.entityInside(state, level, pos, entity, EntityBounding.BOTH);
    }

    default void simulateSuffocation(@Nullable BlockState blockState) {
        MinecraftClient minecraft = MinecraftClient.getInstance();
        GameRenderer renderer = minecraft.gameRenderer;
        InGameOverlayRenderer screenRenderer = ((GameRendererGetter) renderer).getScreenEffectRenderer();
        ((Suffocator) screenRenderer).setSuffocating(blockState);
    }

    default boolean fullyInBounding(Entity entity, BlockPos blockPos, EntityBounding detectionBounding) {
        double x = blockPos.getX() + 0.5d;
        double y = blockPos.getY() + 0.5d;
        double z = blockPos.getZ() + 0.5d;
        Vec3d pos = new Vec3d(x, y, z);
        Vec3d lb = pos.subtract(0.5d, 0.5d, 0.5d); // left bottom
        Vec3d ru = pos.add(0.5d, 0.5d, 0.5d); // right up

        Box bounding = entity.getBoundingBox();
        Vec3d minFeetBounding = bounding.getMinPos();
        Vec3d maxFeetBounding = bounding.getMaxPos().subtract(0.0d, 1.0d, 0.0d);

        boolean minFeetThrough = this.passingThrough(minFeetBounding, lb, ru);
        boolean maxFeetThrough = this.passingThrough(maxFeetBounding, lb, ru);

        Vec3d minHeadBounding = bounding.getMinPos().add(0.0d, 1.0d, 0.0d);
        Vec3d maxHeadBounding = bounding.getMaxPos();

        boolean minHeadThrough = this.passingThrough(minHeadBounding, lb, ru);
        boolean maxHeadThrough = this.passingThrough(maxHeadBounding, lb, ru);

        if (detectionBounding == EntityBounding.FULL_BODY) {
            return (minFeetThrough && maxFeetThrough) && (minHeadThrough && maxHeadThrough);
        } else {
            boolean feetThrough = minFeetThrough && maxFeetThrough && detectionBounding.detectsFeet();
            boolean headThrough = minHeadThrough && maxHeadThrough && detectionBounding.detectsHead();

            return feetThrough || headThrough;
        }
    }

    default boolean fullyInBounding(Entity entity, BlockPos blockPos) {
        return this.fullyInBounding(entity, blockPos, EntityBounding.BOTH);
    }

    default boolean lazyBounding(Entity entity, BlockPos blockPos) {
        double x = blockPos.getX() + 0.5d;
        double y = blockPos.getY() + 0.5d;
        double z = blockPos.getZ() + 0.5d;
        Vec3d pos = new Vec3d(x, y, z);
        Vec3d lb = pos.subtract(0.5d, 0.5d, 0.5d); // left bottom
        Vec3d ru = pos.add(0.5d, 0.5d, 0.5d); // right up

        Box bounding = entity.getBoundingBox();
        Vec3d entityPos = bounding.getCenter();

        return this.touchingPosition(entityPos, lb, ru);
    }

    default boolean shouldSuffocate(Entity entity, BlockPos blockPos) {
        if (!(entity instanceof PlayerEntity)) {
            return false;
        }

        double headDiameter = 0.155d;

        double x = blockPos.getX() + 0.5d;
        double y = blockPos.getY() + 0.5d;
        double z = blockPos.getZ() + 0.5d;
        Vec3d pos = new Vec3d(x, y, z);
        Vec3d lb = pos.subtract(0.5d, 0.5d, 0.5d); // left bottom
        Vec3d ru = pos.add(0.5d, 0.5d, 0.5d); // right up

        Vec3d centerHead = entity.getEyePos();
        return this.passingThrough(centerHead, lb, ru, headDiameter / 2);
    }

    default boolean passingThrough(Vec3d currentPos, Vec3d pos1, Vec3d pos2) {
        return this.passingThrough(currentPos, pos1, pos2, 0.0d);
    }

    default boolean passingThrough(Vec3d currentPos, Vec3d pos1, Vec3d pos2, double tolerance) {
        double minX = pos1.x - tolerance;
        double maxX = pos2.x + tolerance;
        double minY = pos1.y - tolerance;
        double maxY = pos2.y + tolerance;
        double minZ = pos1.z - tolerance;
        double maxZ = pos2.z + tolerance;

        return (currentPos.x >= minX && currentPos.x <= maxX) && (currentPos.y >= minY && currentPos.y <= maxY) && (currentPos.z >= minZ && currentPos.z <= maxZ);
    }

    default boolean touchingPosition(Vec3d currentPos, Vec3d pos1, Vec3d pos2) {
        double minX = pos1.x;
        double maxX = pos2.x;
        double minY = pos1.y;
        double maxY = pos2.y;
        double minZ = pos1.z;
        double maxZ = pos2.z;

        return (currentPos.x >= minX && currentPos.x <= maxX) || (currentPos.y >= minY && currentPos.y <= maxY) || (currentPos.z >= minZ && currentPos.z <= maxZ);
    }

    static void prepareEntity(LivingEntity entity, boolean ongoing) {
        assert entity.isAlive();

        if (ongoing) {
            float currentSpeed = entity.getMovementSpeed();
            transferEntitySpeed.put(entity, currentSpeed);

            entity.setAttached(BackroomsAttachments.LOADING_WORLD, true);
            entity.setMovementSpeed(0);
            entity.setInvulnerable(true);
        } else {
            float transferredSpeed = transferEntitySpeed.get(entity);
            transferEntitySpeed.remove(entity);

            entity.setAttached(BackroomsAttachments.LOADING_WORLD, false);
            entity.setMovementSpeed(transferredSpeed);
            entity.setInvulnerable(false);
        }
    }

    static ServerPlayerEntity.RespawnConfig getSpawnConfig(BlockPos position, float yaw, float pitch, RegistryKey<World> level) {
        return new ServerPlayerEntity(WorldProperties.RespawnData.of(level, position, yaw, pitch), true);
    }

    static void affectPlayer(Entity entity) {
        ServerWorld level = (ServerWorld) entity.getWorld(); // destination
        boolean fullyImmersed = level.getGameRules().get(BackroomsGameRules.FULL_IMMERSION);

        long seed = Random.create().nextLong();
        level.playSound(null, entity, BackroomsSounds.NOCLIP, SoundCategory.PLAYERS, 1.0f, 1.0f, seed);
        if (entity instanceof LivingEntity && fullyImmersed) {
            StatusEffectInstance mobEffect = new StatusEffectInstance(StatusEffects.DARKNESS, 130, 0, false, false, false);
            ((LivingEntity) entity).addEffect(mobEffect);
        }

        if (entity instanceof ServerPlayerEntity && fullyImmersed) {
            entity.setAttached(BackroomsAttachments.LOADING_WORLD, false);
            if (level.dimension() == BackroomsLevels.LEVEL_0) {
                PlayerSnapshot.saveAndClear((ServerPlayerEntity) entity);

                if (entity instanceof ServerPlayerEntity) {
                    ServerPlayerEntity.RespawnConfig respawn = LevelPortal.getSpawnConfig(entity.getBlockPos(), entity.getPitch(), entity.getYaw(), BackroomsLevels.LEVEL_0);
                    ((ServerPlayerEntity) entity).setRespawnPosition(respawn, false);
                }
            } else {
                PlayerSnapshot.restore((ServerPlayerEntity) entity);
            }
        }
    }

    default BackroomsPortalBlock.SpawnLocation selectStartPosition(BlockPos origin, ServerWorld level, @Nullable BlockState updateBlock) {
        Optional<BlockPos> startPosition = Optional.empty();
        Optional<Float> rotation = Optional.empty();
        int y = 50; // from the feet position

        for (int x = 0; x < 16 && startPosition.isEmpty(); x++) {
            for (int z = 0; z < 16 && startPosition.isEmpty(); z++) {
                BlockPos position = origin.offset(x, 0, z).atY(y);
                if (level.isInBuildLimit(position)) {
                    BackroomsMod.LOGGER.info("[BackroomsMod+selectStartPosition] Looking for {}", position);
                    BlockState currentState = level.getBlockState(position);
                    BlockState currentAboveState = level.getBlockState(position.up(1));

                    if (currentState.isReplaceable() && currentAboveState.isReplaceable()) {
                        Direction spawnDirection = LevelPortal.getSpawnDirection(position, level);

                        if (updateBlock != null) {
                            Optional<Direction> hasFacing = updateBlock.getOrEmpty(HorizontalFacingBlock.FACING);
                            if (hasFacing.isPresent()) {
                                updateBlock = updateBlock.setValue(HorizontalFacingBlock.FACING, spawnDirection);
                            }
                        }

                        boolean isValid = this.isPositionValid(position, level, spawnDirection, updateBlock);

                        if (isValid) {
                            BackroomsMod.LOGGER.info("[BackroomsMod+selectStartPosition] Found start for {}", position);
                            startPosition = Optional.of(position);
                            rotation = Optional.of(spawnDirection.asRotation());
                        }
                    }
                }
            }
        }

        return new BackroomsPortalBlock.SpawnLocation(startPosition.orElse(BlockPos.ZERO), rotation.orElse(0.0f));
    }

    private boolean isPositionValid(BlockPos expectedStart, ServerWorld level, Direction spawnDirection, @Nullable BlockState updateBlock) {
        Direction backDirection = spawnDirection.getOpposite();
        BlockPos backSpawn = expectedStart.offset(backDirection.getOffsetX(), backDirection.getOffsetY(), backDirection.getOffsetZ());

        Block currentBlock = level.getBlockState(backSpawn).getBlock();
        Block topBlock = level.getBlockState(backSpawn.up(1)).getBlock();

        boolean isValid = (currentBlock == this.invalidBlock() && topBlock == this.invalidBlock()) || (currentBlock == this && topBlock == this);

        if (isValid && updateBlock != null) {
            level.setBlockState(backSpawn, updateBlock);
            level.setBlockState(backSpawn.up(1), updateBlock);
        }

        return isValid;
    }

    private static Direction getSpawnDirection(BlockPos expectedStart, ServerWorld level) {
        BlockState pxBlock = level.getBlockState(expectedStart.offset(1, 0, 0));
        if (pxBlock.canBeReplaced()) {
            return Direction.EAST;
        }

        BlockState nxBlock = level.getBlockState(expectedStart.offset(-1, 0, 0));
        if (nxBlock.canBeReplaced()) {
            return Direction.WEST;
        }

        BlockState pzBlock = level.getBlockState(expectedStart.offset(0, 0, 1));
        if (pzBlock.canBeReplaced()) {
            return Direction.SOUTH;
        }

        BlockState nzBlock = level.getBlockState(expectedStart.offset(0, 0, -1));
        if (nzBlock.canBeReplaced()) {
            return Direction.NORTH;
        }

        return Direction.EAST;
    }

    @Override
    default int getPortalTransitionTime(ServerWorld level, Entity entity) {
        return 0;
    }

    @Override
    default Transition getLocalTransition() {
        return Transition.NONE;
    }

    Block invalidBlock();
    BlockState suffocatingBlock(BlockState currentState);

    enum EntityBounding {
        HEAD,
        FEET,
        BOTH,
        FULL_BODY;

        public boolean detectsFeet() {
            return this == EntityBounding.FEET || this == EntityBounding.BOTH;
        }

        public boolean detectsHead() {
            return this == EntityBounding.HEAD || this == EntityBounding.BOTH;
        }
    }

    record SpawnLocation(BlockPos position, float xRot, float yRot) {
        public SpawnLocation(BlockPos position, float xRot, float yRot) {
            this.position = position;
            this.xRot = xRot;
            this.yRot = yRot;
        }

        public SpawnLocation(BlockPos position, Float yRot) {
            this(position, 0.0f, yRot);
        }
    }
}
