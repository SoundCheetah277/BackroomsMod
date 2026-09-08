package org.vfast.backrooms.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ClientPacketListener;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vfast.backrooms.BackroomsMod;
import org.vfast.backrooms.blocks.BackroomsBlocks;
import org.vfast.backrooms.client.gui.InvisiScreen;
import org.vfast.backrooms.interfaces.GameRendererGetter;
import org.vfast.backrooms.interfaces.Suffocator;
import org.vfast.backrooms.world.BackroomsLevels;

@Mixin(ClientPacketListener.class)
public abstract class ImmersiveDimensions extends ClientCommonNetworkHandler implements ClientPlayPacketListener, TickablePacketListener {
    @Shadow
    private @Nullable LevelLoadTracker levelLoadTracker;

    protected ImmersiveDimensions(MinecraftClient minecraft, ClientConnection connection, ClientConnectionState cookie) {
        super(minecraft, connection, cookie);
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void clearSuffocation(CallbackInfo ci) {
        if (this.levelLoadTracker == null) {
            this.simulateSuffocation((BlockState) null);
        } else if (this.levelLoadTracker.serverProgress() >= 1.0) {
            this.simulateSuffocation((BlockState) null);
        }
    }

    @Inject(method = "startWaitingForNewLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreenAndShow(Lnet/minecraft/client/gui/screens/Screen;)V"), cancellable = true)
    private void startImmersion(ClientPlayerEntity player, ClientWorld level, LevelLoadingScreen.Reason reason, CallbackInfo ci) {
        this.fullyImmersed(level, reason, ci);
        this.minecraft.gui.setScreen(new InvisiScreen(this.levelLoadTracker));
    }

    @Inject(method = "startWaitingForNewLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LevelLoadingScreen;update(Lnet/minecraft/client/multiplayer/LevelLoadTracker;Lnet/minecraft/client/gui/screens/LevelLoadingScreen$Reason;)V"))
    private void updateImmersion(ClientPlayerEntity player, ClientWorld level, LevelLoadingScreen.Reason reason, CallbackInfo ci) {
        this.fullyImmersed(level, reason, ci);
    }

    @Inject(method = "determineLevelLoadingReason", at = @At(value = "HEAD"), cancellable = true)
    private void levelReason(boolean playerDied, RegistryKey<World> dimensionKey, RegistryKey<World> oldDimensionKey, CallbackInfoReturnable<LevelLoadingScreen.Reason> cir) {
        if (!playerDied && (oldDimensionKey == World.OVERWORLD && dimensionKey == BackroomsLevels.LEVEL_0)) {
            cir.setReturnValue(LevelLoadingScreen.Reason.NETHER_PORTAL); // as long as it's something else than OTHER
        }
    }

    @Unique
    private void fullyImmersed(ClientWorld level, LevelLoadingScreen.Reason reason, CallbackInfo ci) {
        assert this.levelLoadTracker != null;

        if (BackroomsLevels.LEVEL_0.equals(level.dimension()) && reason != LevelLoadingScreen.Reason.OTHER) {
            BackroomsMod.LOGGER.info("[BackroomsMod+ImmersiveDimensions] Prevented screen change");
            this.simulateSuffocation(BackroomsBlocks.MOIST_SILK);
            if (this.levelLoadTracker.isLevelReady()) {
                this.simulateSuffocation((BlockState) null);
            }
            ci.cancel();
        }
    }

    @Unique
    private void simulateSuffocation(@Nullable BlockState blockState) {
        GameRenderer renderer = this.minecraft.gameRenderer;
        InGameOverlayRenderer screenRenderer = ((GameRendererGetter) renderer).getScreenEffectRenderer();
        ((Suffocator) screenRenderer).setSuffocating(blockState);
    }

    @Unique
    private void simulateSuffocation(@Nullable Block block) {
        this.simulateSuffocation(block != null ? block.defaultBlockState() : null);
    }
}
