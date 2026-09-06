package net.fabric_extras.ranged_weapon.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.api.CustomCrossbow;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/// Arm pose `CROSSBOW_HOLD` for any crossbow.
///
/// `require = 0`: Forge-patched 1.20.1 already checks `itemstack.getItem() instanceof CrossbowItem`
/// here, so there is no `isOf` call to wrap and custom crossbows get the pose for free.
@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @WrapOperation(
            method = "getArmPose",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
    )
    private static boolean rwa_armPose_crossbowHold(ItemStack itemStack, Item item, Operation<Boolean> original) {
        if (item == Items.CROSSBOW && CustomCrossbow.instances.contains(itemStack.getItem())) {
            return true;
        }
        return original.call(itemStack, item);
    }
}
