package net.fabric_extras.ranged_weapon.mixin.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.api.RangedWeaponProperties;
import net.fabric_extras.ranged_weapon.internal.MobWeaponUtil;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BowAttackGoal.class)
public class BowAttackGoalMixin {

    @WrapOperation(
            method = "isHoldingBow",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/HostileEntity;isHolding(Lnet/minecraft/item/Item;)Z"))
    private boolean allowCustomBows_RWA(HostileEntity instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || MobWeaponUtil.isHoldingKind(instance, item);
    }

    /**
     * Resolve the charge duration from `ranged_weapon:properties`.
     * Vanilla hardcodes the release check (`useTime >= 20`) and the pull progress curve
     * (`BowItem.getPullProgress`, 20 tick based). Scaling the use time by `20 / pull_time`
     * makes both resolve to the weapon's pull time, with a single hook.
     */
    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/HostileEntity;getItemUseTime()I"))
    private int scaleChargeToPullTime_RWA(HostileEntity instance, Operation<Integer> original) {
        int useTime = original.call(instance);
        var heldBow = instance.getStackInHand(ProjectileUtil.getHandPossiblyHolding(instance, Items.BOW));
        var properties = RangedWeaponProperties.get(heldBow);
        if (properties == null || properties.pull_time() <= 0) {
            return useTime;
        }
        return Math.round(useTime * 20F / properties.pull_time());
    }
}
