package net.fabric_extras.ranged_weapon.mixin.item;

import net.fabric_extras.ranged_weapon.api.RangedConfig;
import net.fabric_extras.ranged_weapon.internal.RangedItemSettings;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Item.Settings.class)
public class ItemSettingMixin implements RangedItemSettings {
    @Unique private RangedConfig rwa_rangedConfig;

    @Override
    public RangedConfig getRangedAttributes() {
        return rwa_rangedConfig;
    }

    @Override
    public Item.Settings rangedAttributes(RangedConfig config) {
        rwa_rangedConfig = config;
        return (Item.Settings) (Object) this;
    }
}
