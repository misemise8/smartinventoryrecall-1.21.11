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

    // Track what was in each slot last tick to detect new pickups
    private final Map<String, Map<Integer, ItemStack>> lastInventoryState = new HashMap<>();

    @Override
    public void onEndTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            processPlayer(player);
        }
    }

    private void processPlayer(ServerPlayerEntity player) {
        // Get snapshot using Data Attachment API
        NbtCompound snapshotNbt = player.getAttached(ModAttachments.INVENTORY_SNAPSHOT);
        if (snapshotNbt == null) {
            // No pending restore, clean up tracking
            lastInventoryState.remove(player.getUuidAsString());
            return;
        }

        InventorySnapshot snapshot = InventorySnapshot.fromNbt(snapshotNbt, player.getRegistryManager());
        if (!snapshot.isPendingRestore()) {
            lastInventoryState.remove(player.getUuidAsString());
            return;
        }

        // Check timeout
        long currentTime = ((ServerWorld) player.getEntityWorld()).getTime();
        if (currentTime - snapshot.getTimestamp() > TIMEOUT_TICKS) {
            SmartInventoryRecall.LOGGER.info("Snapshot expired for player: " + player.getName().getString());
            player.removeAttached(ModAttachments.INVENTORY_SNAPSHOT);
            lastInventoryState.remove(player.getUuidAsString());
            return;
        }

        // Pre-condition: Player must be alive
        if (!player.isAlive())
            return;

        // Pre-condition: No container open
        if (player.currentScreenHandler != player.playerScreenHandler) {
            return;
        }

        Map<Integer, ItemStack> slotMap = snapshot.getSlotMap();
        if (slotMap.isEmpty()) {
            player.removeAttached(ModAttachments.INVENTORY_SNAPSHOT);
            lastInventoryState.remove(player.getUuidAsString());
            return;
        }

        // Perform real-time restoration
        boolean anyRestored = restoreSlotsRealtime(player, slotMap, snapshot);

        // Update snapshot if items were restored
        if (anyRestored) {
            // Check if all items have been restored
            Map<Integer, ItemStack> remainingSlots = snapshot.getSlotMap();
            if (remainingSlots.isEmpty()) {
                SmartInventoryRecall.LOGGER.info("All items restored for player: " + player.getName().getString());
                player.removeAttached(ModAttachments.INVENTORY_SNAPSHOT);
                lastInventoryState.remove(player.getUuidAsString());
            } else {
                // Save updated snapshot
                NbtCompound updatedNbt = snapshot.toNbt(player.getRegistryManager());
                player.setAttached(ModAttachments.INVENTORY_SNAPSHOT, updatedNbt);
            }
        }
    }

    /**
     * Restore items to their original slots in real-time.
     * Scans inventory for items matching snapshot and moves them.
     */
    private boolean restoreSlotsRealtime(ServerPlayerEntity player, Map<Integer, ItemStack> slotMap,
            InventorySnapshot snapshot) {
        PlayerInventory inventory = player.getInventory();
        boolean anyRestored = false;

        // For each slot in the snapshot that still needs restoration
        for (Map.Entry<Integer, ItemStack> entry : new HashMap<>(slotMap).entrySet()) {
            int targetSlot = entry.getKey();
            ItemStack targetStack = entry.getValue();
            Item targetItem = targetStack.getItem();
            int targetCount = targetStack.getCount();

            // Check if target slot is already correct
            ItemStack currentInSlot = inventory.getStack(targetSlot);
            if (!currentInSlot.isEmpty() && currentInSlot.getItem() == targetItem) {
                // Already has the right item, mark as restored
                snapshot.markSlotRestored(targetSlot);
                anyRestored = true;
                continue;
            }

            // Search for matching item in other slots
            for (int i = 0; i < 36; i++) {
                if (i == targetSlot)
                    continue; // Skip target slot

                ItemStack stack = inventory.getStack(i);
                if (stack.isEmpty())
                    continue;

                if (stack.getItem() == targetItem) {
                    // Found matching item! Move it to target slot

                    if (currentInSlot.isEmpty()) {
                        // Target slot is empty - simple move
                        if (stack.getCount() >= targetCount) {
                            ItemStack toMove = stack.split(targetCount);
                            inventory.setStack(targetSlot, toMove);
                        } else {
                            // Not enough count, move what we have
                            inventory.setStack(targetSlot, stack.copy());
                            inventory.setStack(i, ItemStack.EMPTY);
                        }
                    } else {
                        // Target slot has something - swap
                        ItemStack temp = currentInSlot.copy();
                        if (stack.getCount() >= targetCount) {
                            ItemStack toMove = stack.split(targetCount);
                            inventory.setStack(targetSlot, toMove);
                            // Put the displaced item where we took from
                            if (inventory.getStack(i).isEmpty()) {
                                inventory.setStack(i, temp);
                            } else {
                                inventory.insertStack(temp);
                            }
                        }
                    }

                    snapshot.markSlotRestored(targetSlot);
                    anyRestored = true;
                    break; // Move to next target slot
                }
            }
        }

        return anyRestored;
    }
}
