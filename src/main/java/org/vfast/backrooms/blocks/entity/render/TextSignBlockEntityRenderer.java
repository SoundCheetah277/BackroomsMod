package org.vfast.backrooms.blocks.entity.render;

import net.minecraft.client.font.Font;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.vfast.backrooms.blocks.entity.TextSignBlockEntity;

public class TextSignBlockEntityRenderer implements BlockEntityRenderer<TextSignBlockEntity, TextSignBlockEntityRenderState> {
    private final Font font;

    public TextSignBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.font = context.font();
    }

    @Override
    public TextSignBlockEntityRenderState createRenderState() {
        return new TextSignBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(TextSignBlockEntity blockEntity, TextSignBlockEntityRenderState state, float partialTicks, Vec3d cameraPosition, BlockModelRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.frontText = blockEntity.getFrontText();
        state.backText = blockEntity.getBackText();
        state.rotation = blockEntity.getRotation();
    }

    @Override
    public void submit(TextSignBlockEntityRenderState state, MatrixStack matrices, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        this.submitFront(state, matrices, submitNodeCollector);
        this.submitBack(state, matrices, submitNodeCollector);
    }

    private void submitFront(TextSignBlockEntityRenderState state, MatrixStack matrices, SubmitNodeCollector queue) {
        if (state.frontText != null) {
            float width = this.font.width(state.frontText);
            float denominator = width > 10 ? width * 1.3f : width * 2.8f;

            matrices.pushPose();
            this.rotateMatrices(state, matrices, true);
            matrices.scale(1 / denominator, 1 / denominator, 1 / denominator);

            queue.submitText(
                    matrices,
                    -width / 2,
                    -4f,
                    Text.literal(state.frontText).getVisualOrderText(),
                    false,
                    Font.DisplayMode.POLYGON_OFFSET,
                    state.lightCoords,
                    0xfffa3232,
                    0,
                    0xff000000
            );

            matrices.pop();
        }
    }

    private void submitBack(TextSignBlockEntityRenderState state, MatrixStack matrices, SubmitNodeCollector queue) {
        if (state.backText != null) {
            float width = this.font.width(state.backText);
            float denominator = width > 10 ? width * 1.3f : width * 2.8f;

            matrices.push();
            this.rotateMatrices(state, matrices, false);
            matrices.scale(1 / denominator, 1 / denominator, 1 / denominator);

            queue.submitText(
                    matrices,
                    -width / 2,
                    -4f,
                    Text.literal(state.backText).getVisualOrderText(),
                    false,
                    Font.DisplayMode.POLYGON_OFFSET,
                    state.lightCoords,
                    0xfffa3232,
                    0,
                    0xff000000
            );

            matrices.pop();
        }
    }

    private void rotateMatrices(TextSignBlockEntityRenderState state, MatrixStack matrices, boolean isFrontText) {
        switch (state.rotation) {
            case NORTH -> {
                if (isFrontText) {
                    matrices.translate(0.5, 5.5f / 16f, 0.483);
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
                } else {
                    matrices.translate(0.5, 5.5f / 16f, 0.517);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
                }
            }
            case SOUTH -> {
                if (isFrontText) {
                    matrices.translate(0.5, 5.5f / 16f, 0.517);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
                } else {
                    matrices.translate(0.5, 5.5f / 16f, 0.483);
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
                }
            }
            case EAST -> {
                if (isFrontText) {
                    matrices.translate(0.517, 5.5f / 16f, 0.5);
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
                } else {
                    matrices.translate(0.483, 5.5f / 16f, 0.5);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
                }
            }
            case WEST -> {
                if (isFrontText) {
                    matrices.translate(0.483, 5.5f / 16f, 0.5);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
                } else {
                    matrices.translate(0.517, 5.5f / 16f, 0.5);
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
                }
            }
        }
    }
}
