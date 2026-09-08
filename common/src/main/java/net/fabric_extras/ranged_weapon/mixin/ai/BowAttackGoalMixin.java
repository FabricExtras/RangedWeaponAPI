package net.fabric_extras.ranged_weapon.mixin.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.internal.MobWeaponUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.BowAttackGoal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BowAttackGoal.class)
public class BowAttackGoalMixin {

    /// require = 0: Forge 47 patches this to `isHolding(Predicate)` + `instanceof BowItem`,
    /// which accepts custom bows natively (same patch NeoForge carries on 1.21.1).
    @WrapOperation(
            method = "isHoldingBow",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/HostileEntity;isHolding(Lnet/minecraft/item/Item;)Z"))
    private boolean allowCustomBows_RWA(HostileEntity instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || MobWeaponUtil.isHoldingKind(instance, item);
    }

    /// Resolve the charge duration from the weapon's pull time.
    /// Vanilla hardcodes the release check (`useTime >= 20`) and the pull progress curve
    /// (`BowItem.getPullProgress`, 20 tick based). Scaling the use time by `20 / pull_time`
    /// makes both resolve to the weapon's pull time, with a single hook.
    ///
    /// Two variants because the invoke owner differs per platform: the goal's generic bound
    /// is `T extends HostileEntity` in vanilla, widened to `T extends MobEntity` by Forge 47.
    @WrapOperation(
            method = "tick",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/HostileEntity;getItemUseTime()I"))
    private int scaleChargeToPullTime_RWA(HostileEntity instance, Operation<Integer> original) {
        return rwa_scaleToPullTime(instance, original.call(instance));
    }

    @WrapOperation(
            method = "tick",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;getItemUseTime()I"))
    private int scaleChargeToPullTime_Forge_RWA(MobEntity instance, Operation<Integer> original) {
        return rwa_scaleToPullTime(instance, original.call(instance));
    }

    @Unique
    private static int rwa_scaleToPullTime(LivingEntity shooter, int useTime) {
        var heldBow = MobWeaponUtil.heldKind(shooter, Items.BOW);
        if (heldBow == null) {
            return useTime;
        }
        var pullTimeTicks = MobWeaponUtil.pullTimeTicks(shooter, heldBow);
        if (pullTimeTicks <= 0 || pullTimeTicks == 20) {
            return useTime;
        }
        return Math.round(useTime * 20F / pullTimeTicks);
    }
}
