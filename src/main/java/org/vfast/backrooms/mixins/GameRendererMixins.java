package org.vfast.backrooms.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vfast.backrooms.BackroomsMod;
import org.vfast.backrooms.interfaces.GameRendererGetter;
import org.vfast.backrooms.items.BackroomsComponents;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixins implements AutoCloseable, TrackedWaypoint.Projector, GameRendererGetter {
    @Unique
    private static final Identifier VHS_SHADER = Identifier.of(BackroomsMod.ID, "vhs");

    @Shadow
    @Final
    private InGameOverlayRenderer screenEffectRenderer;

    @Shadow
    @Final
    private MinecraftClient minecraft;

    @Shadow
    protected abstract void setPostEffect(Identifier id);

    @Shadow
    public abstract void clearPostEffect();

    @Shadow
    private @Nullable Identifier postEffectId;

    @Override
    public InGameOverlayRenderer getScreenEffectRenderer() {
        return this.screenEffectRenderer;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickVhs(CallbackInfo ci) {
        Hand usedHand = this.minecraft.player.getActiveHand();
        ItemStack stack = this.minecraft.player.getStackInHand(usedHand);
        if (stack.getOrDefault(BackroomsComponents.VHS_COMPONENT, false)) {
            this.setPostEffect(VHS_SHADER);
        } else if (this.postEffectId == VHS_SHADER) {
            this.clearPostEffect();
        }
    }
}
