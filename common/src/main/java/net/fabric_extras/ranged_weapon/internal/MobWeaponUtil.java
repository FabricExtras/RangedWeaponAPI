package net.fabric_extras.ranged_weapon.internal;

import net.fabric_extras.ranged_weapon.api.RangedWeaponProperties;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * Helpers for the AI mixins (`mixin/ai`), letting mob AI recognize custom ranged weapons
 * wherever vanilla hardcodes identity checks against `Items.BOW` / `Items.CROSSBOW`.
 */
public class MobWeaponUtil {

    /**
     * Whether the stack is a custom equivalent of the given vanilla ranged weapon:
     * same weapon kind + carries the `ranged_weapon:properties` component.
     * Intended as the `|| custom` half of wrapped `ItemStack.isOf(...)` checks.
     */
    public static boolean matchesKind(ItemStack stack, Item vanillaItem) {
        if (!stack.contains(RangedWeaponProperties.TYPE)) {
            return false;
        }
        if (vanillaItem == Items.BOW) {
            return stack.getItem() instanceof BowItem;
        }
        if (vanillaItem == Items.CROSSBOW) {
            return stack.getItem() instanceof CrossbowItem;
        }
        return false;
    }

    /**
     * `LivingEntity.isHolding(...)` counterpart of {@link #matchesKind}.
     */
    public static boolean isHoldingKind(LivingEntity entity, Item vanillaItem) {
        return matchesKind(entity.getMainHandStack(), vanillaItem)
                || matchesKind(entity.getOffHandStack(), vanillaItem);
    }

    /**
     * Whether the item carries the `ranged_weapon:properties` component as a default component.
     */
    public static boolean hasProperties(Item item) {
        return item.getComponents().contains(RangedWeaponProperties.TYPE);
    }
}
