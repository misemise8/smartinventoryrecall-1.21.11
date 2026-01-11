package net.SmartInventoryRecall.event;

import net.minecraft.nbt.NbtCompound;

public interface SmartInventoryRecallAccessor {
    void setSmartInventoryRecallSnapshot(NbtCompound nbt);

    NbtCompound getSmartInventoryRecallSnapshot();
}