package org.vfast.backrooms.world.damage;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.vfast.backrooms.BackroomsMod;

public class BackroomsDamageTypes {
    public static final RegistryKey<DamageType> NYCTOPHOBIA = RegistryKey.of(Registries.DAMAGE_TYPE, Identifier.of(BackroomsMod.ID, "nyctophobia"));
}
