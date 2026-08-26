package net.rpg_foundation.ranged_weapon.mixin.item;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Item.Properties.class)
public interface ItemSettingsAccessor {
    @Accessor("components")
    @Nullable DataComponentMap.Builder rwa_getComponents();
}
