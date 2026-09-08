package org.vfast.backrooms.mixins;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vfast.backrooms.blocks.entity.TextSignBlockEntity;
import org.vfast.backrooms.client.gui.TextSignEditScreen;
import org.vfast.backrooms.interfaces.GuiOpener;
import org.vfast.backrooms.world.BackroomsLevels;

import java.util.List;

@Mixin(ClientPlayerEntity.class)
public abstract class LocalPlayerMixins extends AbstractClientPlayerEntity implements GuiOpener {
    @Shadow
    @Final
    protected MinecraftClient minecraft;

    private LocalPlayerMixins(ClientWorld level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Inject(method = "canStartSprinting", at = @At(value = "HEAD"), cancellable = true)
    private void forceStopSprint(CallbackInfoReturnable<Boolean> cir) {
        assert this.minecraft.world != null;

        if (this.minecraft.world.dimension() == BackroomsLevels.LEVEL_0 && !this.getAbilities().invulnerable) {
            this.setSprinting(false);
            cir.setReturnValue(false);
        }
    }

    @Override
    public void openTextSignEdit(TextSignBlockEntity blockEntity, BlockPos pos, boolean isFront) {
        TextSignEditScreen editScreen = new TextSignEditScreen(blockEntity, pos, isFront);
        this.minecraft.gui.setScreen(editScreen);
    }
}
