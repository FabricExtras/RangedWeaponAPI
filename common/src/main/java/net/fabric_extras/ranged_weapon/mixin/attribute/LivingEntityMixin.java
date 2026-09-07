package net.fabric_extras.ranged_weapon.mixin.attribute;

import net.fabric_extras.ranged_weapon.api.CrossbowMechanics;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// `ranged_weapon:haste` speeds up bow/crossbow drawing by inflating the *progress* reported back to
/// vanilla, leaving the item's own max use time untouched.
///
/// 1.20.1 delta: `ItemStack#getMaxUseTime()` is no-arg (1.21.1 takes the `LivingEntity`), and 2.3.4's
/// NeoForge-only partial-tick branch (`RangedHasteEntity`) is gone — Forge 47's `LivingEntity` has the
/// same plain `getItemUseTimeLeft()` as Fabric, so one code path serves both loaders.
///
/// Attaching the attributes themselves happens per-loader, NOT here:
/// Fabric via `createLivingAttributes` (`ranged_weapon_api.fabric.mixins.json`), Forge via
/// `EntityAttributeModificationEvent`.
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow protected int itemUseTimeLeft;
    @Shadow protected ItemStack activeItemStack;

    @Inject(method = "getItemUseTimeLeft", at = @At("HEAD"), cancellable = true)
    private void rwa_getItemUseTimeLeft(CallbackInfoReturnable<Integer> info) {
        var entity = (LivingEntity) (Object) this;
        if (!entity.isUsingItem()) {
            return;
        }
        var useAction = activeItemStack.getUseAction();
        if (useAction != UseAction.BOW && useAction != UseAction.CROSSBOW) {
            return;
        }
        var maxUseTime = activeItemStack.getMaxUseTime();
        var progress = maxUseTime - itemUseTimeLeft;
        var haste = entity.getAttributeValue(EntityAttributes_RangedWeapon.HASTE.attribute);
        var multiplier = EntityAttributes_RangedWeapon.HASTE.asMultiplier((float) haste);
        // Quick Charge's haste half. On 1.21.1 the rebalanced `quick_charge.json` grants
        // `ranged_weapon:haste` `add_multiplied_base 0.10` per level from the `mainhand` slot; there is no
        // enchantment registry to override here, so the same factor is folded in at the one place the
        // attribute is consumed. `add_multiplied_base` multiplies the attribute's post-`ADD_VALUE` total,
        // which is exactly `multiplier` above. See CrossbowMechanics.QuickCharge.
        multiplier *= CrossbowMechanics.QuickCharge.hasteMultiplier(entity.getMainHandStack());
        var newProgress = (int) (progress * multiplier);
        info.setReturnValue(maxUseTime - newProgress);
    }
}
