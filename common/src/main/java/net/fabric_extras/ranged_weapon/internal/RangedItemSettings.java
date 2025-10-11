package net.fabric_extras.ranged_weapon.internal;

import net.fabric_extras.ranged_weapon.api.RangedConfig;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.Item;

public interface RangedItemSettings {
    RangedConfig getRangedAttributes();
    Item.Settings rangedAttributes(RangedConfig config);
    ComponentMap.Builder rwa_getComponentBuilder();
}
