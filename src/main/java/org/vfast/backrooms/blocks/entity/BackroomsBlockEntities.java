package org.vfast.backrooms.blocks.entity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.vfast.backrooms.BackroomsMod;
import org.vfast.backrooms.blocks.BackroomsBlocks;

public class BackroomsBlockEntities {
    public static final BlockEntityType<TextSignBlockEntity> TEXT_SIGN_ENTITY = register("text_ceiling_sign", TextSignBlockEntity::new, BackroomsBlocks.TEXT_SIGN);
    public static final BlockEntityType<TvBlockEntity> CRT_TV_ENTITY = register("crt_tv", TvBlockEntity::new, BackroomsBlocks.CRT_TV);

    private static <T extends BlockEntity> BlockEntityType<T> register(String name, FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory, Block... blocks) {
        Identifier id = Identifier.of(BackroomsMod.ID, name);
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
    }

    public static void registerBlockEntities() {
        BackroomsMod.LOGGER.info("[BackroomsMod] Block entities initialized");
    }
}
