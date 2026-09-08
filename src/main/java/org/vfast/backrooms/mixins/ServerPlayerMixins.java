package org.vfast.backrooms.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vfast.backrooms.attachments.PlayerSnapshot;
import org.vfast.backrooms.blocks.BackroomsBlocks;
import org.vfast.backrooms.interfaces.DarknessDamage;
import org.vfast.backrooms.interfaces.Noclippable;
import org.vfast.backrooms.world.BackroomsGameRules;
import org.vfast.backrooms.world.BackroomsLevels;
import org.vfast.backrooms.world.damage.BackroomsDamageTypes;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixins extends PlayerEntity implements DarknessDamage, Noclippable {
    @Shadow
    public abstract ServerWorld level();

    @Unique
    private int lastNoclipTick = 0;

    @Unique
    private boolean lookingForNoclip = false;

    @Unique
    private int darknessTick = 0;

    @Unique
    private int oldDuration = -1;

    @Unique
    private static final int MINIMUM_LIGHT = 1;

    public ServerPlayerMixins(World level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void backroomsTick(CallbackInfo ci) {
        boolean fullyImmersed = this.level().getGameRules().get(BackroomsGameRules.FULL_IMMERSION);

        if (fullyImmersed) {
            RegistryKey<World> levelKey = this.level().dimension();

            boolean canNoclip = !this.lookingForNoclip && this.tickCount >= this.lastNoclipTick + Noclippable.NOCLIP_TICKS && !this.getAbilities().invulnerable;

            this.tickFood(levelKey);
            if (levelKey == BackroomsLevels.LEVEL_0) {
                this.tickDarkness();
            } else if (levelKey == World.OVERWORLD && canNoclip) {
                this.lookingForNoclip = true;
                this.tickNoclip();
            }
        }
    }

    @Unique
    private void tickNoclip() {
        BlockPos replacer = this.lookAround(this.level(), this.blockPosition());
        assert replacer != null;

        ServerWorld level = this.level();
        level.setBlockAndUpdate(replacer, BackroomsBlocks.FAKE_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(replacer.below(1), BackroomsBlocks.FAKE_BLOCK.defaultBlockState());
        level.setBlockAndUpdate(replacer.below(2), BackroomsBlocks.FAKE_BLOCK.defaultBlockState());

        this.lookingForNoclip = false;
        this.lastNoclipTick = this.tickCount;
    }

    @Override
    @Unique
    public void tickDarkness() {
        if (this.getBlockLight(this) <= ServerPlayerMixins.MINIMUM_LIGHT && !this.getAbilities().invulnerable) {
            this.darknessTick++;
            boolean shouldWarn = this.darknessTick >= DarknessDamage.TICK_PREVENT;
            boolean shouldAttack = this.darknessTick >= DarknessDamage.TICK_ATTACK;
            if (shouldWarn) {
                this.prevent();
                if (shouldAttack) {
                    this.darknessTick = DarknessDamage.TICK_ATTACK;
                    this.attackEntity();
                }
            }
        } else {
            this.darknessTick = Math.max(this.darknessTick - 1, 0);
        }
    }

    @Override
    @Unique
    public void prevent() {
        int effectDuration = this.getDarknessDuration();

        if (this.hasEffect(StatusEffects.DARKNESS) && this.oldDuration != effectDuration) {
            this.oldDuration = effectDuration;
            this.removeEffect(StatusEffects.DARKNESS);
        }

        StatusEffectInstance mobEffect = new StatusEffectInstance(StatusEffects.DARKNESS, effectDuration * 20, 0, false, false, false);
        this.addEffect(mobEffect);
    }

    @Override
    @Unique
    public void performAttack() {
        RegistryKey<World> levelKey = this.level().dimension();

        MinecraftServer server = this.level().getServer();
        assert server != null;

        ServerWorld serverLevel = server.getWorld(levelKey);
        assert serverLevel != null;

        DamageSource levelDamage = new DamageSource(
                this.level().registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .get(BackroomsDamageTypes.NYCTOPHOBIA.identifier()).orElseThrow()
        );

        Vec3 lastMove = this.getDeltaMovement();
        this.hurtServer(serverLevel, levelDamage, 3.0f);
        this.setDeltaMovement(lastMove);
        this.darknessTick = this.darknessTick - DarknessDamage.ATTACK_TICK_RATE;
    }

    @Unique
    private int getDarknessDuration() {
        float preventTick = this.darknessTick - DarknessDamage.TICK_PREVENT;
        assert preventTick >= 0;

        float maxTick = DarknessDamage.TICK_ATTACK - DarknessDamage.TICK_PREVENT;
        float duration = ((preventTick / maxTick) * 65 + 65) / 20;
        return Math.round(duration);
    }

    @Unique
    private void tickFood(RegistryKey<World> dimension) {
        if (BackroomsLevels.isBackrooms(dimension)) {
            this.foodData.setFoodLevel(20);
        }
    }

    @Inject(method = "stopSleepInBed", at = @At(value = "HEAD"))
    private void onFullSleep(boolean forcefulWakeUp, boolean updateLevelList, CallbackInfo ci) {
        boolean fullyImmersed = this.level().getGameRules().get(BackroomsGameRules.FULL_IMMERSION);
        if (forcefulWakeUp && fullyImmersed) {
            PlayerSnapshot.addSleepCount(this);
        }
    }
}
