package net.rpg_foundation.ranged_weapon.mixin.item;

import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Since 26.1 `Item.Properties` no longer holds a component map builder: it accumulates a
 * `DataComponentInitializers.Initializer` chain that runs during resource reload, when the
 * components get bound to the item's registry holder.
 */
@Mixin(Item.Properties.class)
public interface ItemSettingsAccessor {
    @Accessor("componentInitializer")
    DataComponentInitializers.Initializer<Item> rwa_getComponentInitializer();

    @Accessor("componentInitializer")
    void rwa_setComponentInitializer(DataComponentInitializers.Initializer<Item> initializer);
}
