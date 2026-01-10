package net.SmartInventoryRecall.data;

import com.mojang.serialization.DataResult;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InventorySnapshot {
    private static final String KEY_PENDING_RESTORE = "pending_restore";
    private static final String KEY_TIMESTAMP = "timestamp";
    private static final String KEY_SLOTS = "slots";
    private static final String KEY_SLOT_INDEX = "Slot";
    private static final String KEY_ITEM_DATA = "ItemData";

    private final Map<Integer, ItemStack> slotMap = new HashMap<>();
    private long timestamp;
    private boolean pendingRestore;

    public InventorySnapshot() {
        this.timestamp = 0;
        this.pendingRestore = false;
    }

    public void save(Map<Integer, ItemStack> inventory, long currentTime) {
        this.slotMap.clear();
        for (Map.Entry<Integer, ItemStack> entry : inventory.entrySet()) {
            this.slotMap.put(entry.getKey(), entry.getValue().copy());
        }
        this.timestamp = currentTime;
        this.pendingRestore = true;
    }

    public void clear() {
        this.slotMap.clear();
        this.pendingRestore = false;
        this.timestamp = 0;
    }

    public boolean isPendingRestore() {
        return pendingRestore;
    }

    public void setPendingRestore(boolean pendingRestore) {
        this.pendingRestore = pendingRestore;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Map<Integer, ItemStack> getSlotMap() {
        return new HashMap<>(slotMap);
    }

    /**
     * Mark a slot as restored (remove from pending slots).
     */
    public void markSlotRestored(int slot) {
        slotMap.remove(slot);
        if (slotMap.isEmpty()) {
            pendingRestore = false;
        }
    }

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup registries) {
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean(KEY_PENDING_RESTORE, pendingRestore);
        nbt.putLong(KEY_TIMESTAMP, timestamp);

        NbtList slotList = new NbtList();
        for (Map.Entry<Integer, ItemStack> entry : slotMap.entrySet()) {
            NbtCompound itemTag = new NbtCompound();
            itemTag.putInt(KEY_SLOT_INDEX, entry.getKey());

            // Use ItemStack.CODEC with NbtOps for serialization
            DataResult<NbtElement> result = ItemStack.CODEC.encodeStart(
                    registries.getOps(NbtOps.INSTANCE),
                    entry.getValue());
            result.result().ifPresent(element -> itemTag.put(KEY_ITEM_DATA, element));

            slotList.add(itemTag);
        }
        nbt.put(KEY_SLOTS, slotList);
        return nbt;
    }

    public static InventorySnapshot fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        InventorySnapshot snapshot = new InventorySnapshot();
        if (nbt == null)
            return snapshot;

        // Use Optional getters for 1.21.x
        snapshot.pendingRestore = nbt.getBoolean(KEY_PENDING_RESTORE).orElse(false);
        snapshot.timestamp = nbt.getLong(KEY_TIMESTAMP).orElse(0L);

        Optional<NbtList> slotsOpt = nbt.getList(KEY_SLOTS);
        if (slotsOpt.isPresent()) {
            NbtList slotList = slotsOpt.get();
            for (int i = 0; i < slotList.size(); i++) {
                NbtElement element = slotList.get(i);
                if (element instanceof NbtCompound itemTag) {
                    int slot = itemTag.getInt(KEY_SLOT_INDEX).orElse(-1);
                    if (slot >= 0 && slot < 36) {
                        // Use ItemStack.CODEC with NbtOps for deserialization
                        NbtElement itemData = itemTag.get(KEY_ITEM_DATA);
                        if (itemData != null) {
                            DataResult<ItemStack> result = ItemStack.CODEC.parse(
                                    registries.getOps(NbtOps.INSTANCE),
                                    itemData);
                            result.result().ifPresent(stack -> snapshot.slotMap.put(slot, stack));
                        }
                    }
                }
            }
        }
        return snapshot;
    }
}
