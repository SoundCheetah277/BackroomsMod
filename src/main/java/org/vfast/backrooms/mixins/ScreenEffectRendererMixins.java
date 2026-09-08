package org.vfast.backrooms.mixins;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.model.BlockStatesLoader;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vfast.backrooms.interfaces.Suffocator;

@Mixin(InGameOverlayRenderer.class)
public abstract class ScreenEffectRendererMixins implements Suffocator {

    @Shadow
    @Final
    private MinecraftClient minecraft;

    @Shadow
    private static void submitBlockSprite(Sprite sprite, MatrixStack poseStack, SubmitNodeCollector submitNodeCollector, int color) {}

    @Unique
    @Nullable
    private BlockState suffocatingState;

    @Inject(method = "submit", at = @At(value = "HEAD"))
    private void suffocatingIn(boolean isFirstPerson, boolean isSleeping, float partialTicks, SubmitNodeCollector submitNodeCollector, boolean hideGui, CallbackInfo ci) {
        if (this.suffocatingState != null) {
            MatrixStack poseStack = new MatrixStack();
            BlockStatesLoader blockStateModelSet = this.minecraft.getModelManager().getBlockStateModelSet();
            Sprite sprite = blockStateModelSet.getParticleMaterial(this.suffocatingState).sprite();
            submitBlockSprite(sprite, poseStack, submitNodeCollector, -15132391);
        }
    }

    @Unique
    public void setSuffocating(@Nullable BlockState state) {
        this.suffocatingState = state;
    }
}
