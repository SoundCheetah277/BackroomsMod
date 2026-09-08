package org.vfast.backrooms.mixins;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.ContainerUser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vfast.backrooms.world.BackroomsLevels;

@Mixin(PlayerEntity.class)
public abstract class PlayerMixins extends Avatar implements ContainerUser {
    protected PlayerMixins(EntityType<? extends LivingEntity> type, World level) {
        super(type, level);
    }

    @Inject(method = "shouldShowName", at = @At(value = "RETURN"), cancellable = true)
    private void hideName(CallbackInfoReturnable<Boolean> cir) {
        RegistryKey<World> level = this.level().dimension();
        cir.setReturnValue(BackroomsLevels.isBackrooms(level));
    }
}
