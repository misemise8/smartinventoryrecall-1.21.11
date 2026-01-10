package net.SmartInventoryRecall.event;

import net.SmartInventoryRecall.SmartInventoryRecall;
import net.SmartInventoryRecall.data.InventorySnapshot;
import net.SmartInventoryRecall.data.ModAttachments;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles inventory snapshot capture BEFORE death (when inventory is still
 * present).
 * Uses ALLOW_DEATH event which fires before items are dropped.
 */
public class DeathSnapshotHandler implements ServerLivingEntityEvents.AllowDeath {
    @Override
    public boolean allowDeath(LivingEntity entity, DamageSource damageSource, float damageAmount) {
        if (!(entity instanceof ServerPlayerEntity player)) {
            return true; // Allow death for non-players
        }

        // Save inventory snapshot BEFORE death processing
        saveSnapshot(player);

        SmartInventoryRecall.LOGGER.info("Saved inventory snapshot for player: " + player.getName().getString());

        // Always return true - we don't want to prevent death, just save the snapshot
        return true;
    }

    private void saveSnapshot(ServerPlayerEntity player) {
        Map<Integer, ItemStack> items = new HashMap<>();
        PlayerInventory inv = player.getInventory();

        // 0-35 (Hotbar + Main Inventory)
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty()) {
                items.put(i, stack.copy());
            }
        }

        if (items.isEmpty()) {
            SmartInventoryRecall.LOGGER.info("No items to save for player: " + player.getName().getString());
            return; // Nothing to save
        }

        SmartInventoryRecall.LOGGER
                .info("Saving " + items.size() + " slot(s) for player: " + player.getName().getString());

        InventorySnapshot snapshot = new InventorySnapshot();
        snapshot.save(items, ((ServerWorld) player.getEntityWorld()).getTime());

        // Save snapshot using Data Attachment API
        NbtCompound snapshotNbt = snapshot.toNbt(player.getRegistryManager());
        player.setAttached(ModAttachments.INVENTORY_SNAPSHOT, snapshotNbt);
    }
}
