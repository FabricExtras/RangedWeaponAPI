package net.fabric_extras.ranged_weapon.internal;

import net.fabric_extras.ranged_weapon.api.RangedConfig;
import net.minecraft.item.Item;

/// Implemented by a mixin on `Item.Settings`, so a `RangedConfig` can ride along into the item
/// constructor (the item's attribute modifiers are built from it in `RangedWeaponItemMixin`).
///
/// 1.20.1 delta: 2.3.4's `rwa_getComponentBuilder()` is gone — `Item.Settings` has no `ComponentMap.Builder`
/// on this game version, and there is no `AttributeModifiersComponent` to merge into.
public interface RangedItemSettings {
    RangedConfig getRangedAttributes();
    Item.Settings rangedAttributes(RangedConfig config);
}
