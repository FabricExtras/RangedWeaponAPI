package net.rpg_foundation.ranged_weapon.mixin.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.rpg_foundation.ranged_weapon.internal.MobWeaponUtil;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Note: `PillagerEntity.canUseRangedWeapon` is NOT hooked here. On 1.21.11 that method is only ever
 * called from brain tasks (`MeleeAttackTask`, `TargetUtil.isTargetWithinAttackRange`), and pillagers
 * are goal-driven (`initGoals`, no brain), so an override there would be dead code.
 * (`PiglinEntityMixin` does hook it — piglins are brain-driven.)
 */
@Mixin(PillagerEntity.class)
public class PillagerEntityMixin {

    /**
     * `CROSSBOW_HOLD` arm pose for custom crossbows (common code: fixes both logic and rendering).
     * require = 0: NeoForge patches this to `isHolding(Predicate)` + `instanceof CrossbowItem`,
     * which accepts custom crossbows natively.
     */
    @WrapOperation(
            method = "getState",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/PillagerEntity;isHolding(Lnet/minecraft/item/Item;)Z"))
    private boolean allowCustomCrossbows_RWA(PillagerEntity instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || MobWeaponUtil.isHoldingKind(instance, item);
    }
}
