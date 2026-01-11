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
 * Handles inventory snapshot capture BEFORE death (when inventory is still present).
 * Uses ALLOW_DEATH event which fires before items are dropped.
 */
public class DeathSnapshotHandler implements ServerLivingEntityEvents.AllowDeath {
    @Override
    public boolean allowDeath(LivingEntity entity, DamageSource damageSource, float damageAmount) {
        if (!(entity instanceof ServerPlayerEntity player)) {
            return true;
        }

        saveSnapshot(player);
        return true;
    }

    private void saveSnapshot(ServerPlayerEntity player) {
        Map<Integer, ItemStack> items = new HashMap<>();
        PlayerInventory inv = player.getInventory();

        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (!stack.isEmpty()) {
                items.put(i, stack.copy());
            }
        }

        if (items.isEmpty()) {
            return;
        }

        InventorySnapshot snapshot = new InventorySnapshot();
        snapshot.save(items, ((ServerWorld) player.getEntityWorld()).getTime());

        NbtCompound snapshotNbt = snapshot.toNbt(player.getRegistryManager());
        player.setAttached(ModAttachments.INVENTORY_SNAPSHOT, snapshotNbt);

        SmartInventoryRecall.LOGGER.debug("Saved {} slots for {}", items.size(), player.getName().getString());
    }
}