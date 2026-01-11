package net.SmartInventoryRecall.event;

import net.SmartInventoryRecall.SmartInventoryRecall;
import net.SmartInventoryRecall.data.ModAttachments;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

public class RespawnHandler implements ServerPlayerEvents.CopyFrom {
    @Override
    public void copyFromPlayer(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        try {
            NbtCompound snapshot = oldPlayer.getAttached(ModAttachments.INVENTORY_SNAPSHOT);
            if (snapshot != null) {
                newPlayer.setAttached(ModAttachments.INVENTORY_SNAPSHOT, snapshot.copy());
                SmartInventoryRecall.LOGGER.debug("Copied snapshot from old to new player: {}",
                        newPlayer.getName().getString());
            }
        } catch (Exception e) {
            SmartInventoryRecall.LOGGER.error("Failed to copy snapshot for player {}: {}",
                    newPlayer.getName().getString(), e.getMessage());
        }
    }
}