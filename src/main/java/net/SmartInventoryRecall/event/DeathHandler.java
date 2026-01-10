package net.SmartInventoryRecall.event;

import net.SmartInventoryRecall.data.InventorySnapshot;
import net.SmartInventoryRecall.data.ModAttachments;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DeathHandler implements ServerLivingEntityEvents.AfterDeath {
    private static final Random random = new Random();

    @Override
    public void afterDeath(LivingEntity entity, DamageSource damageSource) {
        if (!(entity instanceof ServerPlayerEntity player)) {
            return;
        }

        // Save inventory snapshot
        saveSnapshot(player);

        // Compact dropped items
        compactDroppedItems(player);
    }

    private void saveSnapshot(ServerPlayerEntity player) {
        InventorySnapshot snapshot = new InventorySnapshot();
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
            return; // Nothing to save
        }

        snapshot.save(items, ((ServerWorld) player.getEntityWorld()).getTime());

        // Save snapshot using Data Attachment API
        NbtCompound snapshotNbt = snapshot.toNbt(player.getRegistryManager());
        player.setAttached(ModAttachments.INVENTORY_SNAPSHOT, snapshotNbt);
    }

    /**
     * Compact all dropped items near the death position into a 4-block radius.
     * This runs after death, so items have already spawned.
     */
    private void compactDroppedItems(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        // Search for ItemEntities near the player's death position
        // Use a larger search box to catch scattered items
        Box searchBox = new Box(
                player.getX() - 10, player.getY() - 5, player.getZ() - 10,
                player.getX() + 10, player.getY() + 5, player.getZ() + 10);

        List<ItemEntity> itemEntities = world.getEntitiesByClass(ItemEntity.class, searchBox,
                itemEntity -> {
                    // Filter: Only recently spawned items (less than 1 tick old)
                    // Items have an age field, but it's private. Check if they were just dropped.
                    // Since this runs immediately after death, items should be very new.
                    return itemEntity.age < 20; // Within 1 second of spawning
                });

        double baseX = player.getX();
        double baseY = player.getY() + 0.2;
        double baseZ = player.getZ();

        for (ItemEntity itemEntity : itemEntities) {
            // Calculate random offset within 4 blocks
            double offsetX = (random.nextDouble() * 8.0) - 4.0;
            double offsetZ = (random.nextDouble() * 8.0) - 4.0;

            // Reposition the item
            itemEntity.setPosition(baseX + offsetX, baseY, baseZ + offsetZ);

            // Zero velocity to prevent scattering
            itemEntity.setVelocity(0, 0, 0);
        }
    }
}
