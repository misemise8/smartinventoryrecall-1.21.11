package net.SmartInventoryRecall.mixin;

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
 * Mixin to intercept item drops from players on death and compact them within a
 * 4-block radius.
 */
@Mixin(LivingEntity.class)
public class LivingEntityDropMixin {
    @Unique
    private static final Random smartInventoryRecall_random = new Random();

    @Inject(method = "dropStack(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;", at = @At("RETURN"), cancellable = false)
    private void modifyDropPosition(ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
        LivingEntity self = (LivingEntity) (Object) this;

        // Only apply to players
        if (!(self instanceof PlayerEntity player)) {
            return;
        }

        // Only apply if player is dead (dying)
        if (player.isAlive()) {
            return;
        }

        // Only on server side
        World world = self.getEntityWorld();
        if (world.isClient()) {
            return;
        }

        ItemEntity itemEntity = cir.getReturnValue();
        if (itemEntity == null) {
            return;
        }

        // Compact position: random offset within 4 blocks
        double baseX = player.getX();
        double baseY = player.getY() + 0.2; // Slight lift
        double baseZ = player.getZ();

        double offsetX = (smartInventoryRecall_random.nextDouble() * 8.0) - 4.0; // -4 to +4
        double offsetZ = (smartInventoryRecall_random.nextDouble() * 8.0) - 4.0; // -4 to +4

        itemEntity.setPosition(baseX + offsetX, baseY, baseZ + offsetZ);

        // Zero out velocity to prevent scattering
        itemEntity.setVelocity(0, 0, 0);
    }
}
