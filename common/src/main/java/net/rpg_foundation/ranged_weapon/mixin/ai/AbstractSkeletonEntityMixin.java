package net.rpg_foundation.ranged_weapon.mixin.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.rpg_foundation.ranged_weapon.internal.MobWeaponUtil;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Note: `AbstractSkeletonEntity.canUseRangedWeapon` is NOT hooked here. On 1.21.11 that method is
 * only ever called from brain tasks (`MeleeAttackTask`, `TargetUtil.isTargetWithinAttackRange`),
 * and skeletons are goal-driven (`initGoals`, no brain), so an override there would be dead code.
 */
@Mixin(AbstractSkeletonEntity.class)
public class AbstractSkeletonEntityMixin {

    /**
     * Select the bow attack goal (instead of melee) for custom bows too.
     * require = 0: NeoForge patches this check to `instanceof BowItem`, which accepts custom bows natively.
     */
    @WrapOperation(
            method = "updateAttackType",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private boolean allowCustomBows_RWA(ItemStack stack, Item item, Operation<Boolean> original) {
        return original.call(stack, item) || MobWeaponUtil.matchesKind(stack, item);
    }
}
