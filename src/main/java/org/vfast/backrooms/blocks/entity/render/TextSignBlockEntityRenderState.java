package org.vfast.backrooms.blocks.entity.render;

import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public class TextSignBlockEntityRenderState extends BlockEntityRenderState {
    public @Nullable String frontText;
    public @Nullable String backText;
    public Direction rotation;
}
