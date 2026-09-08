package org.vfast.backrooms.attachments;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PlayerSnapshot {
    private PlayerSnapshot() {}

    // 36 inv + 4 armor + 1 offhand
    private static final int INVENTORY_SIZE = 41;

    public static void saveAndClear(ServerPlayerEntity player) {
        if (PlayerSnapshot.hasSavedData(player)) return;

        player.setAttached(BackroomsAttachments.SAVED_INVENTORY, capture(player));
        player.getInventory().clear();

        ServerPlayerEntity.RespawnConfig respawn = player.getRespawnConfig();
        if (respawn != null && respawn.respawnData().dimension() == World.OVERWORLD) {
            player.setAttached(BackroomsAttachments.SAVED_SPAWN, respawn.respawnData().pos());
        }
    }

    public static void restore(ServerPlayerEntity player) {
        List<ItemStack> snapshot = player.getAttachedOrElse(BackroomsAttachments.SAVED_INVENTORY, Collections.emptyList());
        if (snapshot.isEmpty()) return;

        apply(player, snapshot);
        player.removeAttached(BackroomsAttachments.SAVED_INVENTORY);
        if (player.hasAttached(BackroomsAttachments.SAVED_SPAWN)) {
            BlockPos spawn = player.getAttached(BackroomsAttachments.SAVED_SPAWN);
            if (spawn != null) {
                ServerPlayerEntity.RespawnConfig config = new ServerPlayerEntity.RespawnConfig(WorldProperties.RespawnData.of(World.OVERWORLD, spawn, 0.0f, 0.0f), true);
                player.setRespawnPosition(config, false);
                player.removeAttached(BackroomsAttachments.SAVED_SPAWN);
            }
        }
    }

    public static int addSleepCount(PlayerEntity player) {
        int currentCount = player.getAttachedOrElse(BackroomsAttachments.SLEEP_COUNT, 0);
        player.setAttached(BackroomsAttachments.SLEEP_COUNT, currentCount + 1);
        return currentCount + 1;
    }

    private static List<ItemStack> capture(PlayerEntity player) {
        Inventory inv = player.getInventory();
        List<ItemStack> slots = new ArrayList<>(INVENTORY_SIZE);

        for (ItemStack itemStack : inv) {
            slots.add(itemStack);
        }

        return slots;
    }

    private static void apply(PlayerEntity player, List<ItemStack> slots) {
        Inventory inv = player.getInventory();
        inv.clear();

        for (int i = 0; i < inv.size(); i++) {
            ItemStack item = slots.get(i).copy();
            inv.setStack(i, item);
        }
    }

    private static boolean hasSavedData(PlayerEntity player) {
        return player.hasAttached(BackroomsAttachments.SAVED_INVENTORY);
    }
}
