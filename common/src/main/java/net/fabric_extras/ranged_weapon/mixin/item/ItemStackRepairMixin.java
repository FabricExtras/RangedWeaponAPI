package net.fabric_extras.ranged_weapon.mixin.item;

import net.fabric_extras.ranged_weapon.api.CustomBow;
import net.fabric_extras.ranged_weapon.api.CustomCrossbow;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * `Item.canRepair` was removed in 1.21.2 (repairing is driven by the `minecraft:repairable` component).
 * Custom bows/crossbows keep accepting their supplied repair ingredient on top of that.
 */
@Mixin(ItemStack.class)
public class ItemStackRepairMixin {
    @Inject(method = "canRepairWith", at = @At("HEAD"), cancellable = true)
    private void customRepairIngredient_RWA(ItemStack ingredient, CallbackInfoReturnable<Boolean> cir) {
        var item = ((ItemStack) (Object) this).getItem();
        if (item instanceof CustomBow bow) {
            if (bow.getRepairIngredientSupplier().get().test(ingredient)) {
                cir.setReturnValue(true);
            }
        } else if (item instanceof CustomCrossbow crossbow) {
            if (crossbow.getRepairIngredientSupplier().get().test(ingredient)) {
                cir.setReturnValue(true);
            }
        }
    }
}
