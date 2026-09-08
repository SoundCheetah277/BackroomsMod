package org.vfast.backrooms.mixins;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vfast.backrooms.items.BackroomsItems;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixins <AvatarlikeEntity extends Avatar & ClientAvatarEntity> extends LivingEntityRenderer<AvatarlikeEntity, AvatarRenderState, PlayerEntityModel> {
    public AvatarRendererMixins(EntityRendererFactory.Context context, PlayerEntityModel model, float shadow) {
        super(context, model, shadow);
    }

    @Inject(method = "getArmPose(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;", at = @At(value = "RETURN"), cancellable = true)
    private static void camcorderPose(Avatar avatar, ItemStack itemInHand, Hand hand, CallbackInfoReturnable<BipedEntityModel.ArmPose> cir) {
        BipedEntityModel.ArmPose itemArmPose = getItemArmPose(itemInHand.getUseAction());
        if (ItemStack.areItemsEqual(BackroomsItems.CAMCORDER.getDefaultStack(), itemInHand) && !avatar.swinging && itemArmPose != null) {
            cir.setReturnValue(itemArmPose);
        }
    }

    @Unique
    private static BipedEntityModel.@Nullable ArmPose getItemArmPose(ItemUseAnimation useAnimation) {
        switch (useAnimation) {
            case CROSSBOW -> {
                return BipedEntityModel.ArmPose.CROSSBOW_HOLD;
            }

            case BOW -> {
                return BipedEntityModel.ArmPose.BOW_AND_ARROW;
            }

            case BLOCK -> {
                return BipedEntityModel.ArmPose.BLOCK;
            }

            case SPYGLASS -> {
                return BipedEntityModel.ArmPose.SPYGLASS;
            }

            case TRIDENT -> {
                return BipedEntityModel.ArmPose.THROW_TRIDENT;
            }
        }

        return null;
    }
}
