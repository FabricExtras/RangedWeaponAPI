package net.rpg_foundation.ranged_weapon.mixin.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.rpg_foundation.ranged_weapon.internal.MobWeaponUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The charge duration needs no hook here: the goal charges via `CrossbowItem.getPullTime`,
 * which already resolves the `ranged_weapon:properties` pull time (see item.CrossbowItemMixin).
 */
@Mixin(RangedCrossbowAttackGoal.class)
public class CrossbowAttackGoalMixin {
    /**
     * require = 0: NeoForge patches this to `isHolding(Predicate)` + `instanceof CrossbowItem`,
     * which accepts custom crossbows natively.
     */
    @WrapOperation(
            method = "isHoldingCrossbow",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Monster;isHolding(Lnet/minecraft/world/item/Item;)Z"))
    private boolean allowCustomCrossbows_RWA(Monster instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || MobWeaponUtil.isHoldingKind(instance, item);
    }
}
