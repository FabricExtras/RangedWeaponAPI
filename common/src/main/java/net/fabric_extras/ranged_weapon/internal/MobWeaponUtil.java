package net.fabric_extras.ranged_weapon.internal;

import net.fabric_extras.ranged_weapon.api.CustomRangedWeapon;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.minecraft.entity.LivingEntity;
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
