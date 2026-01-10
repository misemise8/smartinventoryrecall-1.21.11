package net.SmartInventoryRecall.data;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

/**
 * Attachment types for Smart Inventory Recall mod.
 * Uses Fabric Data Attachment API for persistent player data.
 */
public class ModAttachments {
    public static final AttachmentType<NbtCompound> INVENTORY_SNAPSHOT = AttachmentRegistry.createPersistent(
            Identifier.of("smartinventoryrecall", "inventory_snapshot"),
            NbtCompound.CODEC);

    public static void init() {
        // Registration happens on class load, this method just ensures the class is
        // loaded
    }
}
