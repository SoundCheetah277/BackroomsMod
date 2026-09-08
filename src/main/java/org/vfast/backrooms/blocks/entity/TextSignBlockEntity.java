package org.vfast.backrooms.blocks.entity;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.vfast.backrooms.blocks.interfaces.CeilingSupportSign;
import org.vfast.backrooms.blocks.interfaces.TextBlockEntity;

import java.util.Objects;

public class TextSignBlockEntity extends BlockEntity implements TextBlockEntity {
    private String frontText;
    private String backText;
    private Direction blockRotation;

    public static final EnumProperty<Direction> ROTATION = Properties.HORIZONTAL_FACING;

    public TextSignBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BackroomsBlockEntities.TEXT_SIGN_ENTITY, worldPosition, blockState);
        this.frontText = "F";
        this.backText = "B";
        this.blockRotation = blockState.getValue(ROTATION);
    }

    public String getFrontText() {
        return this.frontText;
    }

    public String getBackText() {
        return this.backText;
    }

    @Override
    public int maxTextWidth() {
        return 75;
    }

    public Direction getRotation() {
        return this.blockRotation;
    }

    @Override
    protected void saveAdditional(final ValueOutput output) {
        super.saveAdditional(output);
        output.store("front_text", Codec.STRING, this.frontText);
        output.store("back_text", Codec.STRING, this.backText);
    }

    @Override
    protected void loadAdditional(final ValueInput input) {
        super.loadAdditional(input);
        this.frontText = input.read("front_text", Codec.STRING).orElse("");
        this.backText = input.read("back_text", Codec.STRING).orElse("");
    }

    public boolean setBackText(final String text) {
        if (!Objects.equals(text, this.backText)) {
            this.backText = text;
            this.markUpdated();
            return true;
        } else {
            return false;
        }
    }

    public boolean setFrontText(final String text) {
        if (!Objects.equals(text, this.frontText)) {
            this.frontText = text;
            this.markUpdated();
            return true;
        } else {
            return false;
        }
    }

    public void updateText(String text, boolean isFrontText) {
        if (isFrontText) {
            this.setFrontText(text);
        } else {
            this.setBackText(text);
        }
    }

    private void markUpdated() {
        this.setChanged();
        assert this.level != null;
        this.level.sendBlockUpdated(this.getPos(), this.getCachedState(), this.getCachedState(), 3);
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
