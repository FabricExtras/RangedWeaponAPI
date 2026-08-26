package net.testmod;

import net.rpg_foundation.ranged_weapon.api.CustomBow;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;

public class TestMod implements ModInitializer {
    public static final String NAMESPACE = "testmod";
    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        var id = Identifier.fromNamespaceAndPath(NAMESPACE, "custom_longbow");
        var bow = new CustomBow(
                new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, id))
                        .durability(300)
                        .repairable(ItemTags.GOLD_TOOL_MATERIALS), // repair is the vanilla component now
                new RangedWeaponConfig(9, 40, 1F, null)
        );
        Registry.register(
                BuiltInRegistries.ITEM,
                id,
                bow
        );
    }
}
