package net.rpg_foundation.ranged_weapon.mixin.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponProperties;
import net.rpg_foundation.ranged_weapon.internal.MobWeaponUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RangedBowAttackGoal.class)
public class BowAttackGoalMixin {

    /**
     * require = 0: NeoForge patches this to `isHolding(Predicate)` + `instanceof BowItem`,
     * which accepts custom bows natively.
     */
    @WrapOperation(
            method = "isHoldingBow",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Monster;isHolding(Lnet/minecraft/world/item/Item;)Z"))
    private boolean allowCustomBows_RWA(Monster instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || MobWeaponUtil.isHoldingKind(instance, item);
    }

    /**
     * Resolve the charge duration from `ranged_weapon:properties`.
     * Vanilla hardcodes the release check (`useTime >= 20`) and the pull progress curve
     * (`BowItem.getPullProgress`, 20 tick based). Scaling the use time by `20 / pull_time`
     * makes both resolve to the weapon's pull time, with a single hook.
     *
     * ### The `require = 0` pairing — read before touching either variant
     *
     * `BowAttackGoal<T extends HostileEntity & RangedAttackMob>` holds its `actor` in a field of
     * type `T`, so the `getItemUseTime()` call in `tick` is compiled against the erasure of `T`.
     * Vanilla erases to `HostileEntity`; NeoForge widens the bound so it erases to `MobEntity`.
     * The invoke *owner* in the bytecode therefore differs per loader, and a single `@WrapOperation`
     * cannot describe both. Hence two variants, each `require = 0`:
     * **exactly one is expected to match per loader, and at least one must.** If a future mapping
     * or patch changes the owner again, both silently fail to apply and mob draw speed quietly
     * reverts to vanilla's 20 ticks — no mixin error, no crash. `detectMissingPullTimeHook_RWA`
     * below exists purely to turn that silent regression into a WARN in the log.
     */
    @WrapOperation(
            method = "tick",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Monster;getTicksUsingItem()I"))
    private int scaleChargeToPullTime_RWA(Monster instance, Operation<Integer> original) {
        return rwa_scaleToPullTime(instance, original.call(instance));
    }

    @WrapOperation(
            method = "tick",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;getTicksUsingItem()I"))
    private int scaleChargeToPullTime_Neo_RWA(Mob instance, Operation<Integer> original) {
        return rwa_scaleToPullTime(instance, original.call(instance));
    }

    private static int rwa_scaleToPullTime(LivingEntity shooter, int useTime) {
        rwa_pullTimeHookFired = true;
        var heldBow = shooter.getItemInHand(ProjectileUtil.getWeaponHoldingHand(shooter, Items.BOW));
        var properties = RangedWeaponProperties.get(heldBow);
        if (properties == null || properties.pull_time() <= 0) {
            return useTime;
        }
        return Math.round(useTime * 20F / properties.pull_time());
    }

    // --- Safety net for the `require = 0` pairing above ---

    private static volatile boolean rwa_pullTimeHookFired = false;
    private static boolean rwa_pullTimeHookWarned = false;
    private static int rwa_bowGoalTicks = 0;

    /**
     * `tick` only runs while some mob is actively running a bow attack goal, and any tick spent
     * drawing with the target in sight goes through `getItemUseTime()`. So if this has ticked a
     * long while and neither `@WrapOperation` variant ever fired, neither applied — warn once.
     * Costs one static int increment per bow-goal tick until it settles.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void detectMissingPullTimeHook_RWA(CallbackInfo ci) {
        if (rwa_pullTimeHookFired || rwa_pullTimeHookWarned) {
            return;
        }
        if (++rwa_bowGoalTicks > 200) {
            rwa_pullTimeHookWarned = true;
            LogUtils.getLogger().warn("Ranged Weapon API: mob draw-speed hook did not apply"
                    + " (neither BowAttackGoal#getItemUseTime @WrapOperation variant matched);"
                    + " mobs will draw custom bows at the vanilla 20 tick rate.");
        }
    }
}
