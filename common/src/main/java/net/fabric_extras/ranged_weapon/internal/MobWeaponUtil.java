package net.fabric_extras.ranged_weapon.internal;

import net.fabric_extras.ranged_weapon.api.CustomRangedWeapon;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;

/// Helpers for the AI mixins (`mixin/ai`), letting mob AI recognize custom ranged weapons
/// wherever vanilla hardcodes identity checks against `Items.BOW` / `Items.CROSSBOW`.
///
/// 1.20.1 delta: 3.x tests for the `ranged_weapon:properties` data component. There are no data
/// components here, but the equivalent marker already exists: `mixin.item.RangedWeaponItemMixin`
/// implements {@link CustomRangedWeapon} on `RangedWeaponItem` and gives *every* bow/crossbow a
/// `RangedConfig` (the weapon-type baseline when none was supplied) — exactly what 3.x's
/// `BowItemMixin`/`CrossbowItemMixin` constructor hooks do with the default component. So
/// `instanceof CustomRangedWeapon` is the 1.20.1 spelling of "carries the properties component".
public class MobWeaponUtil {

    /// Whether the stack is a custom equivalent of the given vanilla ranged weapon:
    /// same weapon kind + participates in the ranged weapon systems.
    /// Intended as the `|| custom` half of wrapped `ItemStack.isOf(...)` checks.
    public static boolean matchesKind(ItemStack stack, Item vanillaItem) {
        var item = stack.getItem();
        if (!(item instanceof CustomRangedWeapon)) {
            return false;
        }
        if (vanillaItem == Items.BOW) {
            return item instanceof BowItem;
        }
        if (vanillaItem == Items.CROSSBOW) {
            return item instanceof CrossbowItem;
        }
        return false;
    }

    /// `LivingEntity.isHolding(...)` counterpart of {@link #matchesKind}.
    public static boolean isHoldingKind(LivingEntity entity, Item vanillaItem) {
        return matchesKind(entity.getMainHandStack(), vanillaItem)
                || matchesKind(entity.getOffHandStack(), vanillaItem);
    }

    /// The held stack matching {@link #matchesKind}, main hand first — the hand resolution order
    /// `ProjectileUtil.getHandPossiblyHolding` uses. Null when neither hand holds one.
    @Nullable
    public static ItemStack heldKind(LivingEntity entity, Item vanillaItem) {
        var mainHand = entity.getMainHandStack();
        if (matchesKind(mainHand, vanillaItem)) {
            return mainHand;
        }
        var offHand = entity.getOffHandStack();
        if (matchesKind(offHand, vanillaItem)) {
            return offHand;
        }
        return null;
    }

    /// Whether the item participates in the ranged weapon systems.
    /// 1.20.1 counterpart of 3.x's "has the `ranged_weapon:properties` default component".
    public static boolean hasProperties(Item item) {
        return item instanceof CustomRangedWeapon;
    }

    /// The **firing** step of mob crossbow combat, for a mob holding a *custom* crossbow.
    ///
    /// `CrossbowUser.shoot(LivingEntity, float)` — the default method `PillagerEntity.attack` and
    /// `PiglinEntity.attack` delegate to — guards its shot with `entity.isHolding(Items.CROSSBOW)` on
    /// 1.20.1 vanilla (1.21.1 vanilla already tests `instanceof CrossbowItem`, which is why RWA 3.x ships
    /// no hook here). Without this, a mob holding a custom crossbow charges, reaches `READY_TO_ATTACK`,
    /// calls `attack(...)` — and nothing is spawned; the goal/task then resets the charged flag and the mob
    /// loops silently forever.
    ///
    /// The guard cannot be wrapped where it lives, because it sits in a **default method of an interface**
    /// and only newer Mixin builds tolerate an injector there. Fabric Loader 0.19.5 ships Mixin 0.8.7, which
    /// applies it; Forge 47.3.0 ships Mixin **0.8.5**, which rejects the whole mixin at PREPARE with
    /// `InvalidInterfaceMixinException: Interface mixin contains a non-public method!` (and refuses injectors
    /// on interface mixins outright at APPLY even once the method is made public). A mixin rejected at
    /// PREPARE never evaluates `require`, which is how the earlier `ai.CrossbowUserMixin` went unnoticed.
    ///
    /// So the two `attack` call sites host the fix instead — a plain `@Inject` on a class, which every Mixin
    /// build applies — and this method performs the shot vanilla's guard skipped. Mirrors
    /// `CrossbowUser#shoot(LivingEntity, float)` exactly apart from the guard.
    ///
    /// Returns true when it fired, i.e. when the caller must **not** run vanilla's `shoot`; false when the
    /// mob holds a plain vanilla crossbow (or no crossbow at all), leaving vanilla authoritative. Forge 47
    /// patches the guard to `isHolding(is -> is.getItem() instanceof CrossbowItem)` and so would have fired
    /// too — this produces the identical shot (same `CrossbowItem.shootAll`, same hand, RWA already hooks
    /// `getHandPossiblyHolding`), and because the caller cancels vanilla's `shoot`, nothing fires twice.
    public static boolean shootCustomCrossbow(CrossbowUser user, float speed) {
        var entity = (LivingEntity) user;
        if (entity.isHolding(Items.CROSSBOW) || !isHoldingKind(entity, Items.CROSSBOW)) {
            return false;
        }
        var hand = ProjectileUtil.getHandPossiblyHolding(entity, Items.CROSSBOW);
        var stack = entity.getStackInHand(hand);
        CrossbowItem.shootAll(entity.getWorld(), entity, hand, stack, speed,
                14 - entity.getWorld().getDifficulty().getId() * 4);
        user.postShoot();
        return true;
    }

    /// The pull time (in ticks) the shooter experiences with the given weapon.
    ///
    /// Reads the shooter's `ranged_weapon:pull_time` attribute — the same source the player path uses
    /// (`mixin.item.BowItemMixin#rwa_getPullProgress`), so a mob draws a given bow at exactly the speed
    /// a player would. The weapon contributes to that attribute through its hand-slot modifiers, which
    /// `LivingEntity` applies to mobs too. Falls back to the weapon's own `RangedConfig` if the entity
    /// somehow lacks the attribute.
    public static int pullTimeTicks(LivingEntity shooter, ItemStack weapon) {
        var instance = shooter.getAttributeInstance(EntityAttributes_RangedWeapon.PULL_TIME.attribute);
        if (instance != null) {
            return Math.max(1, (int) Math.round(instance.getValue() * 20));
        }
        if (weapon.getItem() instanceof CustomRangedWeapon rangedWeapon) {
            return rangedWeapon.getRangedWeaponConfig().pullTimeTicks();
        }
        return 20;
    }
}
