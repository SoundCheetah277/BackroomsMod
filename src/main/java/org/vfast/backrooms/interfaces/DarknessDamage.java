package org.vfast.backrooms.interfaces;


import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public interface DarknessDamage {
    int TICK_PREVENT = 30;
    int TICK_ATTACK = 700; // 35 seconds
    int ATTACK_TICK_RATE = 25;

    default int getBlockLight(LivingEntity entity) {
        World level = entity.getWorld();
        return level.getLightingProvider().getLight(entity.getBlockPos(), 999); // remove skylight
    }

    default void attackEntity() {
        if (this.shouldPreventAndAttack()) {
            this.prevent();
        }
        this.performAttack();
    }

    default boolean shouldPreventAndAttack() {
        return true;
    }

    void tickDarkness();
    void prevent();
    void performAttack();
}
