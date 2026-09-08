package org.vfast.backrooms.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.vfast.backrooms.BackroomsMod;

public record LimitedChatPacket(String content) implements CustomPayload {
    public static final Identifier LIMITED_CHAT_PACKET_ID = Identifier.of(BackroomsMod.ID, "limited_chat");
    public static final CustomPayload.Type<LimitedChatPacket> TYPE = new CustomPayload.Type<>(LIMITED_CHAT_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, LimitedChatPacket> CODEC = PacketCodec.tuple(PacketCodecs.STRING, LimitedChatPacket::content, LimitedChatPacket::new);

    @Override
    public Type<? extends CustomPayload> type() {
        return TYPE;
    }
}
