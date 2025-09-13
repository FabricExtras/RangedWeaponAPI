package net.fabric_extras.ranged_weapon.mixin.item;

import net.fabric_extras.ranged_weapon.api.RangedConfig;
import net.fabric_extras.ranged_weapon.internal.RangedItemSettings;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.Settings.class)
public class ItemSettingMixin implements RangedItemSettings {
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
}
