package org.vfast.backrooms.world;

import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.vfast.backrooms.BackroomsMod;

import java.util.List;

public class BackroomsLevels {
    private static final List<RegistryKey<World>> BACKROOM_LEVELS;
    public static final RegistryKey<World> LEVEL_0 = RegistryKey.of(Registries.DIMENSION, Identifier.of(BackroomsMod.ID, "level_0"));

    public static boolean isBackrooms(RegistryKey<World> level) {
        return BACKROOM_LEVELS.contains(level);
    }

    static {
        BACKROOM_LEVELS = List.of(BackroomsLevels.LEVEL_0);
    }
}
