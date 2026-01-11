package net.SmartInventoryRecall.event;

import net.SmartInventoryRecall.data.ModAttachments;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

import java.util.List;
import java.util.Random;

/**
 * Handles drop compaction AFTER death (when items have spawned).
 * Uses AFTER_DEATH event to reposition dropped items.
 */
public class DeathDropHandler implements ServerLivingEntityEvents.AfterDeath {
    private static final Random random = new Random();

    @Override
    public void afterDeath(LivingEntity entity, DamageSource damageSource) {
        if (!(entity instanceof ServerPlayerEntity player)) {
            return;
        }

        // Compact dropped items
        compactDroppedItems(player);
    }

    /**
     * Compact all dropped items near the death position into a 4-block radius.
     */
    private void compactDroppedItems(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        // Search for ItemEntities near the player's death position
        Box searchBox = new Box(
                player.getX() - 10, player.getY() - 5, player.getZ() - 10,
                player.getX() + 10, player.getY() + 5, player.getZ() + 10);

        List<ItemEntity> itemEntities = world.getEntitiesByClass(ItemEntity.class, searchBox,
                itemEntity -> itemEntity.age < 20); // Recently spawned items

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
