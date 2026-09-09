package net.fabric_extras.ranged_weapon.mixin.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.internal.MobWeaponUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinEntity.class)
public class PiglinEntityMixin {

    /// `CROSSBOW_HOLD` pose for custom crossbows (common code: fixes both logic and rendering).
    /// require = 0: Forge 47 patches this to `isHolding(Predicate)` + `instanceof CrossbowItem`,
    /// which accepts custom crossbows natively.
    @WrapOperation(
            method = "getActivity",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/PiglinEntity;isHolding(Lnet/minecraft/item/Item;)Z"))
    private boolean allowCustomCrossbows_RWA(PiglinEntity instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || MobWeaponUtil.isHoldingKind(instance, item);
    }

    @Inject(method = "canUseRangedWeapon", at = @At("HEAD"), cancellable = true)
    private void canUseCustomCrossbows_RWA(RangedWeaponItem weapon, CallbackInfoReturnable<Boolean> cir) {
        if (weapon instanceof CrossbowItem && MobWeaponUtil.hasProperties(weapon)) {
            cir.setReturnValue(true);
        }
    }

    /// Piglins value custom crossbows for pickup/equip decisions, same as vanilla ones
    /// (wraps all 3 `isOf(Items.CROSSBOW)` checks in the method; Forge 47 leaves them alone).
    @WrapOperation(
            method = "prefersNewEquipment",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private boolean valueCustomCrossbows_RWA(ItemStack stack, Item item, Operation<Boolean> original) {
        return original.call(stack, item) || MobWeaponUtil.matchesKind(stack, item);
    }

    /// The **firing** step. `attack` is a one-liner delegating to `CrossbowUser.shoot(this, 1.6F)`, whose
    /// 1.20.1-vanilla guard is `isHolding(Items.CROSSBOW)` — a custom crossbow never gets past it, so the
    /// piglin charges, fires nothing, and loops forever. Wrapping the guard where it lives needs an injector on
    /// an **interface mixin**, which Forge 47's Mixin 0.8.5 rejects outright (the old `ai.CrossbowUserMixin`,
    /// deleted), so the fix is hosted here instead — a plain `@Inject` on a class: shoot it ourselves and
    /// cancel. {@link MobWeaponUtil#shootCustomCrossbow} returns false (→ vanilla runs) for a plain crossbow.
    @Inject(method = "attack(Lnet/minecraft/entity/LivingEntity;F)V", at = @At("HEAD"), cancellable = true)
    private void shootCustomCrossbows_RWA(LivingEntity target, float pullProgress, CallbackInfo ci) {
        if (MobWeaponUtil.shootCustomCrossbow((PiglinEntity) (Object) this, 1.6F)) {
            ci.cancel();
        }
    }
}
