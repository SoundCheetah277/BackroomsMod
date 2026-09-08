package org.vfast.backrooms.blocks.entity;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.vfast.backrooms.blocks.TvBlock;

public class TvBlockEntity extends BlockEntity {
    private int lastPoweredTick;

    public TvBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BackroomsBlockEntities.CRT_TV_ENTITY, worldPosition, blockState);
        this.lastPoweredTick = 0;
    }

    public int getLastPoweredTick() {
        return this.lastPoweredTick;
    }

    public int addPoweredTick() {
        this.lastPoweredTick = Math.min(this.lastPoweredTick + 1, TvBlock.SOUND_DURATION);
        this.markUpdated();
        return this.lastPoweredTick;
    }

    public int resetPoweredTick() {
        this.lastPoweredTick = 0;
        this.markUpdated();
        return 0;
    }

    public boolean isComplete() {
        return this.lastPoweredTick >= TvBlock.SOUND_DURATION - 1 || this.lastPoweredTick <= 0;
    }

    private void markUpdated() {
        this.setChanged();
        assert this.level != null;
        this.level.sendBlockUpdated(this.getPos(), this.getCachedState(), this.getCachedState(), 3);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("song_tick", Codec.INT, this.lastPoweredTick);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.lastPoweredTick = input.read("song_tick", Codec.INT).orElse(-1);
    }

    @Override
    public NbtCompound getUpdateTag(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    @Override
    public Packet<ClientPlayPacketListener> getUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}
