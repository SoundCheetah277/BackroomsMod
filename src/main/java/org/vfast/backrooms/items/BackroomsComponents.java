package org.vfast.backrooms.items;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.vfast.backrooms.BackroomsMod;

public class BackroomsComponents {
    public static final ComponentType<Boolean> VHS_COMPONENT = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(BackroomsMod.ID, "vhs_effect"),
            ComponentType.<Boolean>builder().codec(Codec.BOOL).build()
    );

    public static void registerComponents() {
        BackroomsMod.LOGGER.info("[BackroomsMod] Initialized components");
    }
}
