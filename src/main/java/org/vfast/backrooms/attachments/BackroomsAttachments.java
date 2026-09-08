package org.vfast.backrooms.attachments;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.vfast.backrooms.BackroomsMod;

import java.util.List;

public class BackroomsAttachments {
    public static final AttachmentType<List<ItemStack>> SAVED_INVENTORY = AttachmentRegistry.create(
            Identifier.of(BackroomsMod.ID, "saved_inventory"),
            builder -> builder
                    .persistent(ItemStack.OPTIONAL_CODEC.listOf())
                    .syncWith(ItemStack.OPTIONAL_LIST_PACKET_CODEC, AttachmentSyncPredicate.targetOnly())
                    .copyOnDeath()
    );

    public static final AttachmentType<BlockPos> SAVED_SPAWN = AttachmentRegistry.create(
            Identifier.of(BackroomsMod.ID, "saved_spawn"),
            builder -> builder
                    .persistent(BlockPos.CODEC)
                    .syncWith(BlockPos.PACKET_CODEC, AttachmentSyncPredicate.targetOnly())
                    .copyOnDeath()
    );

    public static final AttachmentType<Boolean> LOADING_WORLD = AttachmentRegistry.create(
            Identifier.of(BackroomsMod.ID, "loading_world"),
            builder -> builder.initializer(() -> false)
    );

    public static final AttachmentType<Integer> SLEEP_COUNT = AttachmentRegistry.create(
            Identifier.of(BackroomsMod.ID, "sleep_count"),
            builder -> builder
                    .persistent(Codec.INT)
                    .syncWith(PacketCodecs.INTEGER, AttachmentSyncPredicate.targetOnly())
                    .copyOnDeath()
    );
}
