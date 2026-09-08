package org.vfast.backrooms.world.generation;


import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.*;
import net.minecraft.structure.processor.BlockIgnoreStructureProcessor;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import java.util.List;

public class CaptainRuthiePieces {

    /**
     * How many blocks below the surface the structure top should sit.
     * 8 means the top of the template is buried 9 blocks underground.
     */
    private static final int BURIAL_DEPTH = 9;

    /**
     * The Y level used as the initial placement anchor before surface-snapping.
     * Matches the value chosen in CaptainRuthieStructure#generatePieces.
     */
    private static final int GENERATION_HEIGHT = 90;

    /** NBT structure template location: assets/backrooms/structures/overworld/captain_ruthie.nbt */
    private static final Identifier STRUCTURE_LOCATION =
            Identifier.of("backrooms", "overworld/captain_ruthie");

    /**
     * The rotation pivot of your template. Adjust X/Y/Z to match the centre of
     * your .nbt file (same role as IglooPieces.PIVOTS). A common default is the
     * centre of the structure's bounding box; set to (0, 0, 0) if unsure and
     * tweak after testing in-game.
     */
    private static final BlockPos PIVOT = new BlockPos(0, 0, 0);

    // -------------------------------------------------------------------------

    public static void addPieces(final StructureTemplateManager structureTemplateManager, final BlockPos position, final BlockRotation rotation, final StructurePiecesHolder structurePieceAccessor) {
        structurePieceAccessor.addPiece(
                new CaptainRuthiePieces.CaptainRuthiePiece(structureTemplateManager, position, rotation)
        );
    }

    // -------------------------------------------------------------------------

    public static class CaptainRuthiePiece extends SimpleStructurePiece {

        public CaptainRuthiePiece(final StructureTemplateManager structureTemplateManager, final BlockPos position, final BlockRotation rotation) {
            super(BackroomsPieceTypes.CAPTAIN_RUTHIE_PIECE, 0, structureTemplateManager, STRUCTURE_LOCATION, STRUCTURE_LOCATION.toString(), makeSettings(rotation), position);
        }

        /** Deserialization constructor — called when loading a saved chunk. */
        public CaptainRuthiePiece(final StructureTemplateManager structureTemplateManager, final NbtCompound tag) {
            super(
                    BackroomsPieceTypes.CAPTAIN_RUTHIE_PIECE,
                    tag,
                    structureTemplateManager,
                    ignored -> makeSettings(tag.read("Rot", BlockRotation.LEGACY_CODEC).orElseThrow())
            );
        }

        // -- Helpers ----------------------------------------------------------

        private static StructurePlacementData makeSettings(final BlockRotation rotation) {
            return new StructurePlacementData()
                    .setRotation(rotation)
                    .setMirror(BlockMirror.NONE)
                    .setRotationPivot(PIVOT)
                        .addProcessor(BlockIgnoreStructureProcessor.STRUCTURE_BLOCK)
                    .setLiquidSettings(StructureLiquidSettings.IGNORE_WATERLOGGING);
        }

        // -- Serialization ----------------------------------------------------

        @Override
        protected void addAdditionalSaveData(final StructureContext context, final NbtCompound tag) {
            super.addAdditionalSaveData(context, tag);
            tag.store("Rot", BlockRotation.LEGACY_CODEC, this.placeSettings.getRotation());
        }

        // -- Data markers (add chest / spawner logic here if needed) ----------

        @Override
        protected void handleDataMarker(final String markerId, final BlockPos position, final ServerWorldAccess level, final Random random, final BlockBox chunkBB) {
            // Add any data-marker handling here (e.g. loot chests, mob spawners).
            // Leave empty if your template has no data markers.
        }

        // -- Placement --------------------------------------------------------

        @Override
        public void postProcess(final StructureWorldAccess level, final StructureAccessor structureManager, final ChunkGenerator generator, final Random random, final StructureSpawns.BoundingBox chunkBB, final ChunkPos chunkPos, final BlockPos referencePos) {
            // Snap to the actual world surface at the structure's XZ position,
            // then bury the top of the template BURIAL_DEPTH blocks underground.
            int surfaceY = level.getHeight(Types.WORLD_SURFACE_WG, this.templatePosition.getX(), this.templatePosition.getZ());

            // Store the original template position so we can restore it after
            // placement (same pattern as IglooPieces).
            BlockPos savedPosition = this.templatePosition;

            // Offset from the placeholder Y (GENERATION_HEIGHT) to
            // (surfaceY - BURIAL_DEPTH), so the top of the template sits
            // BURIAL_DEPTH blocks below the ground surface.
            this.templatePosition = this.templatePosition.offset(0, surfaceY - GENERATION_HEIGHT - BURIAL_DEPTH, 0);

            super.postProcess(level, structureManager, generator, random, chunkBB, chunkPos, referencePos);

            // Restore original position so saved NBT remains consistent.
            this.templatePosition = savedPosition;
        }
    }
}