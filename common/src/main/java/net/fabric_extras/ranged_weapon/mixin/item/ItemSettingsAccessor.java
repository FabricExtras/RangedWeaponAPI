package net.fabric_extras.ranged_weapon.mixin.item;

import net.minecraft.component.ComponentMap;
import net.minecraft.item.Item;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.Settings.class)
public interface ItemSettingsAccessor {
    @Accessor("components")
    @Nullable ComponentMap.Builder rwa_getComponents();
}
