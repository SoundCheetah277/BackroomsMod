package org.vfast.backrooms.world;

import net.minecraft.util.Identifier;
import org.vfast.backrooms.BackroomsMod;

public class BackroomsGameRules {
    public static final GameRule<Boolean> FULL_IMMERSION = GameRuleBuilder
            .forBoolean(true)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Identifier.of(BackroomsMod.ID, "full_immersion"));

    public static final GameRule<Boolean> LIMITED_CHATTING = GameRuleBuilder
            .forBoolean(true)
            .category(GameRuleCategory.CHAT)
            .buildAndRegister(Identifier.of(BackroomsMod.ID, "limited_chatting"));

    public static void registerGameRules() {
        BackroomsMod.LOGGER.info("[BackroomsMod] GameRules initialized");
    }
}
