package org.vfast.backrooms.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.vfast.backrooms.BackroomsMod;
import org.vfast.backrooms.blocks.interfaces.TextBlockEntity;
import org.vfast.backrooms.world.BackroomsGameRules;

import java.util.List;

public class BackroomsNetworking {
    public static void registerNetwork() {
        BackroomsNetworking.registerPackets();
        BackroomsNetworking.registerReceivers();

        BackroomsMod.LOGGER.info("[BackroomsMod] Networking registered!");
    }

    private static void registerPackets() {
        PayloadTypeRegistry.serverboundPlay().register(UpdateTextSignPacket.TYPE, UpdateTextSignPacket.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(LimitedChatPacket.TYPE, LimitedChatPacket.CODEC);
    }

    private static void registerReceivers() {
        // update text sign
        ServerPlayNetworking.registerGlobalReceiver(UpdateTextSignPacket.TYPE, (payload, context) -> {
            ServerWorld level = context.player().level();
            BlockEntity be = level.getBlockEntity(payload.pos());

            if (be instanceof TextBlockEntity) {
                ((TextBlockEntity) be).updateText(payload.text(), payload.isFrontText());
            } else {
                BackroomsMod.LOGGER.warn("[BackroomsMod] UpdateTextSignPacket used on non-TextBlockEntity");
            }
        });

        // limited chat
        ServerPlayNetworking.registerGlobalReceiver(LimitedChatPacket.TYPE, (payload, context) -> {
            if (!context.player().level().getGameRules().get(BackroomsGameRules.LIMITED_CHATTING)) {
                context.player().connection.sendDisguisedChatMessage(Text.literal(payload.content()), MessageType.bind(MessageType.CHAT, context.player()));
                return;
            }

            BlockPos senderPos = context.player().blockPosition();
            float radius = 30.0f;

            List<ServerPlayerEntity> receivers = context.player().level().getEntitiesOfClass(
                    ServerPlayerEntity.class,
                    new Box(senderPos).inflate(radius),
                    player -> player.distanceToSqr(Vec3d.atCenterOf(senderPos)) <= radius * radius
            );

            if (receivers.size() == 1) {
                receivers.getFirst().sendSystemMessage(Text.translatable("message.backrooms.chat_alone").withStyle(Formatting.DARK_GRAY, Formatting.ITALIC));
            } else {
                for (ServerPlayerEntity receiver : receivers) {
                    receiver.connection.sendDisguisedChatMessage(Text.literal(payload.content()), MessageType.bind(MessageType.CHAT, context.player()));
                }
            }
        });
    }
}
