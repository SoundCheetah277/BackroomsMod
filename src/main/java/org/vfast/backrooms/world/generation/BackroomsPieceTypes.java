package org.vfast.backrooms.world.generation;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.StructureType;
import org.vfast.backrooms.BackroomsMod;

public class BackroomsPieceTypes {
    public static final StructureType<CaptainRuthieStructure> CAPTAIN_RUTHIE = Registry.register(
            Registries.STRUCTURE_TYPE,
            Identifier.of("backrooms", "captain_ruthie"),
            () -> CaptainRuthieStructure.CODEC
    );

    public static final StructurePieceType.ManagerAware CAPTAIN_RUTHIE_PIECE = Registry.register(
            Registries.STRUCTURE_PIECE,
            Identifier.of("backrooms", "captain_ruthie"),
            CaptainRuthiePieces.CaptainRuthiePiece::new
    );

    public static void registerStructures() {
        BackroomsMod.LOGGER.info("[BackroomsMod] Structures initialized");
    }
}
