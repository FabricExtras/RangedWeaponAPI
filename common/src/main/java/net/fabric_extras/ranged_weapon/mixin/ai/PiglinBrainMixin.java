package net.fabric_extras.ranged_weapon.mixin.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.internal.MobWeaponUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/// Piglin kiting: keep backing away from a target that closes to melee range while holding a crossbow.
///
/// `PiglinBrain.isHoldingCrossbow` gates the `AttackTask.create(5, 0.75F)` entry in the FIGHT activity
/// list — the "strafe backwards if the target is within 5 blocks" behaviour. Without it a piglin with a
/// custom crossbow charges and fires correctly but walks into melee range and additionally tries to
/// punch, because dropping out of that branch lets `RangedApproachTask` and `MeleeAttackTask` run.
///
/// **Beyond parity, deliberately.** This site is byte-identical on 1.21.1 and equally unhandled by
/// RWA 3.x, so this is an improvement over the reference line rather than a port of it. The 1.21.1
/// branch wants the same one-line fix.
///
/// require = 0: Forge 47 patches this to `isHolding(Predicate)` + `instanceof CrossbowItem`.
@Mixin(PiglinBrain.class)
public class PiglinBrainMixin {
    @WrapOperation(
            method = "isHoldingCrossbow",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isHolding(Lnet/minecraft/item/Item;)Z"))
    private static boolean allowCustomCrossbows_RWA(LivingEntity instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || MobWeaponUtil.isHoldingKind(instance, item);
    }
}
