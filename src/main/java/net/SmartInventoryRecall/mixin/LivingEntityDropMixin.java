package net.SmartInventoryRecall.mixin;

import net.SmartInventoryRecall.SmartInventoryRecall;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

/**
 * Mixin to intercept item drops from players on death and compact them within a 2-block radius.
 */
@Mixin(LivingEntity.class)
public class LivingEntityDropMixin {
    @Unique
    private static final Random smartInventoryRecall_random = new Random();
    @Unique
    private static final double COMPACT_RADIUS = 2.0;

    @Inject(method = "dropStack(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;", at = @At("RETURN"))
    private void modifyDropPosition(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (!(self instanceof PlayerEntity player) || player.isAlive()) {
            return;
        }

        World world = self.getEntityWorld();
        if (world.isClient()) {
            return;
        }

        ItemEntity itemEntity = cir.getReturnValue();
        if (itemEntity == null) {
            return;
        }

        try {
            double baseX = player.getX();
            double baseY = player.getY() + 0.2;
            double baseZ = player.getZ();

            double offsetX = (smartInventoryRecall_random.nextDouble() * COMPACT_RADIUS * 2.0) - COMPACT_RADIUS;
            double offsetZ = (smartInventoryRecall_random.nextDouble() * COMPACT_RADIUS * 2.0) - COMPACT_RADIUS;

            itemEntity.setPosition(baseX + offsetX, baseY, baseZ + offsetZ);
            itemEntity.setVelocity(0, 0, 0);
        } catch (Exception e) {
            SmartInventoryRecall.LOGGER.warn("Failed to modify drop position: {}", e.getMessage());
        }
    }
}