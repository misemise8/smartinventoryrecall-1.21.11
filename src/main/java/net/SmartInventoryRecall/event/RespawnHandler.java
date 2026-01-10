package net.SmartInventoryRecall.event;

import net.SmartInventoryRecall.data.ModAttachments;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

public class RespawnHandler implements ServerPlayerEvents.CopyFrom {
    @Override
    public void copyFromPlayer(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        // Copy snapshot from old player to new player using Data Attachment API
        NbtCompound snapshot = oldPlayer.getAttached(ModAttachments.INVENTORY_SNAPSHOT);
        if (snapshot != null) {
            newPlayer.setAttached(ModAttachments.INVENTORY_SNAPSHOT, snapshot.copy());
        }
    }
}
