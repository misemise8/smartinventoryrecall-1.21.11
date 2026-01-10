package net.SmartInventoryRecall;

import net.SmartInventoryRecall.data.ModAttachments;
import net.SmartInventoryRecall.event.DeathDropHandler;
import net.SmartInventoryRecall.event.DeathSnapshotHandler;
import net.SmartInventoryRecall.event.RespawnHandler;
import net.SmartInventoryRecall.event.RestoreTickHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartInventoryRecall implements ModInitializer {
	public static final String MOD_ID = "smartinventoryrecall";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Smart Inventory Recall initializing...");

		// Initialize Data Attachments
		ModAttachments.init();

		// Register event handlers
		// ALLOW_DEATH: Capture inventory BEFORE items drop
		ServerLivingEntityEvents.ALLOW_DEATH.register(new DeathSnapshotHandler());

		// AFTER_DEATH: Compact dropped items
		ServerLivingEntityEvents.AFTER_DEATH.register(new DeathDropHandler());

		// COPY_FROM: Copy snapshot on respawn
		ServerPlayerEvents.COPY_FROM.register(new RespawnHandler());

		// END_SERVER_TICK: Restore slots when items recovered
		ServerTickEvents.END_SERVER_TICK.register(new RestoreTickHandler());

		LOGGER.info("Smart Inventory Recall initialized.");
	}
}