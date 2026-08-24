package net.fabric_extras.ranged_weapon.mixin.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.internal.MobWeaponUtil;
import net.minecraft.entity.ai.goal.CrossbowAttackGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The charge duration needs no hook here: the goal charges via `CrossbowItem.getPullTime`,
 * which already resolves the `ranged_weapon:properties` pull time (see item.CrossbowItemMixin).
 */
@Mixin(CrossbowAttackGoal.class)
public class CrossbowAttackGoalMixin {
    /**
     * require = 0: NeoForge patches this to `isHolding(Predicate)` + `instanceof CrossbowItem`,
     * which accepts custom crossbows natively.
     */
    @WrapOperation(
            method = "isEntityHoldingCrossbow",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/HostileEntity;isHolding(Lnet/minecraft/item/Item;)Z"))
    private boolean allowCustomCrossbows_RWA(HostileEntity instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || MobWeaponUtil.isHoldingKind(instance, item);
    }
}
