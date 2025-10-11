package net.fabric_extras.ranged_weapon.mixin.item;

import net.fabric_extras.ranged_weapon.api.RangedConfig;
import net.fabric_extras.ranged_weapon.internal.RangedItemSettings;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Item.Settings.class)
public class ItemSettingMixin implements RangedItemSettings {
    @Shadow private ComponentMap.Builder components;
    private RangedConfig rangedConfig;

    @Override
    public RangedConfig getRangedAttributes() {
        return rangedConfig;
    }

    @Override
    public Item.Settings rangedAttributes(RangedConfig config) {
        rangedConfig = config;
        return (Item.Settings) (Object) this;
    }

    @Override
    public ComponentMap.Builder rwa_getComponentBuilder() {
        return components;
    }
}
