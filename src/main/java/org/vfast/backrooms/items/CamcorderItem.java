package org.vfast.backrooms.items;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.vfast.backrooms.sounds.BackroomsSounds;

public class CamcorderItem extends Item {
    public CamcorderItem(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResult use(World level, PlayerEntity player, Hand hand) {
        player.swingHand(hand);
        ItemStack stack = player.getStackInHand(hand);
        stack.set(BackroomsComponents.VHS_COMPONENT, !stack.getOrDefault(BackroomsComponents.VHS_COMPONENT, false));

//        if (!level.isClientSide()) {
//            level.playPlayerSound(BackroomsSounds.CAMERA_CLICK); TODO: FINISH THIS BITCH ASS
//        }

        return ActionResult.SUCCESS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.CROSSBOW;
    }
}
