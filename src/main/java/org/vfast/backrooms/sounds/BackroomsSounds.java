package org.vfast.backrooms.sounds;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.vfast.backrooms.BackroomsMod;

public class BackroomsSounds {
    public static final RegistryEntry<SoundEvent> LIGHT_BUZZING = registerSound("light_buzzing");
    public static final RegistryEntry<SoundEvent> SHALLOW_BUZZING = registerSound("shallow_buzzing");
    public static final RegistryEntry<SoundEvent> MONSTER_NOISE = registerSound("monster_noise");
    public static final RegistryEntry<SoundEvent> NOCLIP = registerSound("noclip");
    public static final RegistryEntry<SoundEvent> NOCLIP_SMALL = registerSound("noclip_small");
    public static final RegistryEntry<SoundEvent> TV_SONG = registerSound("tv_song");

    private static RegistryEntry<SoundEvent> registerSound(String id) {
        Identifier identifier = Identifier.of(BackroomsMod.ID, id);
        return Registry.registerForRegistryEntry(Registries.SOUND_EVENT, identifier, SoundEvent.createVariableRangeEvent(identifier));
    }

    public static void registerSounds() {
        BackroomsMod.LOGGER.info("[BackroomsMod] Sounds initialized");
    }
}
