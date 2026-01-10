package net.SmartInventoryRecall.mixin;

import net.SmartInventoryRecall.event.SmartInventoryRecallAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements SmartInventoryRecallAccessor {
    @Unique
    private NbtCompound smartInventoryRecall_snapshot;

    @Override
    public void setSmartInventoryRecallSnapshot(NbtCompound nbt) {
        this.smartInventoryRecall_snapshot = nbt;
    }

    @Override
    public NbtCompound getSmartInventoryRecallSnapshot() {
        return this.smartInventoryRecall_snapshot;
    }

    // Inject into writeNbt to persist data
    // Signature: NbtCompound writeNbt(NbtCompound nbt)
    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void writeSmartInventoryRecallData(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
        if (this.smartInventoryRecall_snapshot != null) {
            nbt.put("smart_inventory_recall", this.smartInventoryRecall_snapshot);
        }
    }

    // Inject into readNbt to load data
    // Signature: void readNbt(NbtCompound nbt)
    @Inject(method = "readNbt", at = @At("TAIL"))
    private void readSmartInventoryRecallData(NbtCompound nbt, CallbackInfo ci) {
        Optional<NbtCompound> opt = nbt.getCompound("smart_inventory_recall");
        opt.ifPresent(compound -> this.smartInventoryRecall_snapshot = compound);
    }
}
