package org.vfast.backrooms.world.generation;

import com.mojang.serialization.MapCodec;
import net.minecraft.structure.StructurePiecesCollector;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureType;

import java.util.Optional;

public class CaptainRuthieStructure extends Structure {

    public static final MapCodec<CaptainRuthieStructure> CODEC = createCodec(CaptainRuthieStructure::new);

    public CaptainRuthieStructure(final Config settings) {
        super(settings);
    }

    @Override
    public Optional<StructurePosition> findGenerationPoint(final Context context) {
        return getStructurePosition(context, Types.WORLD_SURFACE_WG, builder -> this.generatePieces(builder, context));
    }

    private void generatePieces(final StructurePiecesCollector builder, final Context context) {
        ChunkPos chunkPos = context.chunkPos();
        ChunkRandom random = context.random();
        // Y = 90 is a placeholder; postProcess in CaptainRuthiePieces will snap
        // the piece to the actual surface and then apply the 8-block burial offset.
        BlockPos startPos = new BlockPos(chunkPos.getStartX(), 90, chunkPos.getStartZ());
        BlockRotation rotation = BlockRotation.random(random);
        CaptainRuthiePieces.addPieces(context.structureTemplateManager(), startPos, rotation, builder);
    }

    @Override
    public StructureType<CaptainRuthieStructure> type() {
        return BackroomsPieceTypes.CAPTAIN_RUTHIE;
    }
}