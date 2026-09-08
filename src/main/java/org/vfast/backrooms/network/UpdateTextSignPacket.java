package org.vfast.backrooms.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.vfast.backrooms.BackroomsMod;

public record UpdateTextSignPacket(BlockPos pos, Boolean isFrontText, String text) implements CustomPayload {
    public static final Identifier UPDATE_TEXT_SIGN_PACKET_ID = Identifier.of(BackroomsMod.ID, "update_text_sign");
    public static final CustomPayload.Type<UpdateTextSignPacket> TYPE = new CustomPayload.Type<>(UPDATE_TEXT_SIGN_PACKET_ID);
    public static final PacketCodec<RegistryByteBuf, UpdateTextSignPacket> CODEC = PacketCodec.tuple(BlockPos.PACKET_CODEC, UpdateTextSignPacket::pos, ByteBufCodecs.BOOL, UpdateTextSignPacket::isFrontText, ByteBufCodecs.STRING_UTF8, UpdateTextSignPacket::text, UpdateTextSignPacket::new);

    @Override
    public Type<? extends CustomPayload> type() {
        return TYPE;
    }
}
