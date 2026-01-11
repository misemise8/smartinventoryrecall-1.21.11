package net.SmartInventoryRecall.event;

import net.SmartInventoryRecall.SmartInventoryRecall;
import net.SmartInventoryRecall.data.InventorySnapshot;
import net.SmartInventoryRecall.data.ModAttachments;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles real-time slot restoration as items are picked up.
 * Items go directly to their original slots when picked up.
 */
public class RestoreTickHandler implements ServerTickEvents.EndTick {
    private static final long TIMEOUT_TICKS = 6000L; // 5 minutes

    @Override
    public void onEndTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            try {
                processPlayer(player);
            } catch (Exception e) {
                SmartInventoryRecall.LOGGER.error("Error processing player {}: {}",
                        player.getName().getString(), e.getMessage());
            }
        }
    }

    private void processPlayer(ServerPlayerEntity player) {
        NbtCompound snapshotNbt = player.getAttached(ModAttachments.INVENTORY_SNAPSHOT);
        if (snapshotNbt == null) {
            return;
        }

        InventorySnapshot snapshot;
        try {
            snapshot = InventorySnapshot.fromNbt(snapshotNbt, player.getRegistryManager());
        } catch (Exception e) {
            SmartInventoryRecall.LOGGER.error("Failed to deserialize snapshot for player {}: {}",
                    player.getName().getString(), e.getMessage());
            player.removeAttached(ModAttachments.INVENTORY_SNAPSHOT);
            return;
        }

        if (!snapshot.isPendingRestore()) {
            return;
        }

        long currentTime = ((ServerWorld) player.getEntityWorld()).getTime();
        if (currentTime - snapshot.getTimestamp() > TIMEOUT_TICKS) {
            SmartInventoryRecall.LOGGER.info("Snapshot expired for player: {}", player.getName().getString());
            player.removeAttached(ModAttachments.INVENTORY_SNAPSHOT);
            return;
        }

        if (!player.isAlive() || player.currentScreenHandler != player.playerScreenHandler) {
            return;
        }

        Map<Integer, ItemStack> slotMap = snapshot.getSlotMap();
        if (slotMap.isEmpty()) {
            player.removeAttached(ModAttachments.INVENTORY_SNAPSHOT);
            return;
        }

        boolean anyRestored = restoreSlotsRealtime(player, slotMap, snapshot);

        if (anyRestored) {
            Map<Integer, ItemStack> remainingSlots = snapshot.getSlotMap();
            if (remainingSlots.isEmpty()) {
                SmartInventoryRecall.LOGGER.info("All items restored for player: {}", player.getName().getString());
                player.removeAttached(ModAttachments.INVENTORY_SNAPSHOT);
            } else {
                try {
                    NbtCompound updatedNbt = snapshot.toNbt(player.getRegistryManager());
                    player.setAttached(ModAttachments.INVENTORY_SNAPSHOT, updatedNbt);
                } catch (Exception e) {
                    SmartInventoryRecall.LOGGER.error("Failed to save updated snapshot for player {}: {}",
                            player.getName().getString(), e.getMessage());
                }
            }
        }
    }

    private boolean restoreSlotsRealtime(ServerPlayerEntity player, Map<Integer, ItemStack> slotMap, InventorySnapshot snapshot) {
        PlayerInventory inventory = player.getInventory();
        boolean anyRestored = false;

        for (Map.Entry<Integer, ItemStack> entry : new HashMap<>(slotMap).entrySet()) {
            int targetSlot = entry.getKey();
            ItemStack targetStack = entry.getValue();
            Item targetItem = targetStack.getItem();
            int targetCount = targetStack.getCount();

            try {
                ItemStack currentInSlot = inventory.getStack(targetSlot);
                if (!currentInSlot.isEmpty() && currentInSlot.getItem() == targetItem) {
                    snapshot.markSlotRestored(targetSlot);
                    anyRestored = true;
                    continue;
                }

                for (int i = 0; i < 36; i++) {
                    if (i == targetSlot) continue;

                    ItemStack stack = inventory.getStack(i);
                    if (stack.isEmpty() || stack.getItem() != targetItem) {
                        continue;
                    }

                    if (currentInSlot.isEmpty()) {
                        if (stack.getCount() >= targetCount) {
                            ItemStack toMove = stack.split(targetCount);
                            inventory.setStack(targetSlot, toMove);
                        } else {
                            inventory.setStack(targetSlot, stack.copy());
                            inventory.setStack(i, ItemStack.EMPTY);
                        }
                    } else {
                        ItemStack temp = currentInSlot.copy();
                        if (stack.getCount() >= targetCount) {
                            ItemStack toMove = stack.split(targetCount);
                            inventory.setStack(targetSlot, toMove);
                            if (inventory.getStack(i).isEmpty()) {
                                inventory.setStack(i, temp);
                            } else {
                                inventory.insertStack(temp);
                            }
                        }
                    }

                    snapshot.markSlotRestored(targetSlot);
                    anyRestored = true;
                    break;
                }
            } catch (Exception e) {
                SmartInventoryRecall.LOGGER.warn("Failed to restore slot {} for player {}: {}",
                        targetSlot, player.getName().getString(), e.getMessage());
            }
        }

        return anyRestored;
    }
}