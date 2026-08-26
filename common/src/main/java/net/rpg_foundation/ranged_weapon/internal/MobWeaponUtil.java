package net.rpg_foundation.ranged_weapon.internal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponProperties;

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
        if (stack.get(RangedWeaponProperties.TYPE) == null) { // not contains(): NeoForge's Yarn jar names it `has` → NoSuchMethodError
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
        return matchesKind(entity.getMainHandItem(), vanillaItem)
                || matchesKind(entity.getOffhandItem(), vanillaItem);
    }

    /**
     * Whether the item carries the `ranged_weapon:properties` component as a default component.
     */
    public static boolean hasProperties(Item item) {
        return item.components().get(RangedWeaponProperties.TYPE) != null; // not contains(): NeoForge's Yarn jar names it `has`
    }
}
