package net.SmartInventoryRecall.event;

import net.SmartInventoryRecall.SmartInventoryRecall;
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
    private static final double COMPACT_RADIUS = 2.0;
    private static final double SEARCH_RADIUS = 10.0;

    @Override
    public void afterDeath(LivingEntity entity, DamageSource damageSource) {
        if (!(entity instanceof ServerPlayerEntity player)) {
            return;
        }

        try {
            compactDroppedItems(player);
        } catch (Exception e) {
            SmartInventoryRecall.LOGGER.error("Failed to compact items for player {}: {}",
                    player.getName().getString(), e.getMessage());
        }
    }

    /**
     * Compact all dropped items near the death position into a 2-block radius.
     */
    private void compactDroppedItems(ServerPlayerEntity player) {
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        Box searchBox = new Box(
                player.getX() - SEARCH_RADIUS, player.getY() - 5, player.getZ() - SEARCH_RADIUS,
                player.getX() + SEARCH_RADIUS, player.getY() + 5, player.getZ() + SEARCH_RADIUS
        );

        List<ItemEntity> itemEntities = world.getEntitiesByClass(ItemEntity.class, searchBox,
                itemEntity -> itemEntity.age < 20);

        if (itemEntities.isEmpty()) {
            return;
        }

        double baseX = player.getX();
        double baseY = player.getY() + 0.2;
        double baseZ = player.getZ();

        int itemCount = 0;
        for (ItemEntity itemEntity : itemEntities) {
            try {
                double offsetX = (random.nextDouble() * COMPACT_RADIUS * 2.0) - COMPACT_RADIUS;
                double offsetZ = (random.nextDouble() * COMPACT_RADIUS * 2.0) - COMPACT_RADIUS;

                itemEntity.setPosition(baseX + offsetX, baseY, baseZ + offsetZ);
                itemEntity.setVelocity(0, 0, 0);
                itemCount++;
            } catch (Exception e) {
                SmartInventoryRecall.LOGGER.warn("Failed to reposition item: {}", e.getMessage());
            }
        }

        SmartInventoryRecall.LOGGER.info("Compacted {} items for player: {}",
                itemCount, player.getName().getString());
    }
}