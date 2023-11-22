package net.testmod;

import net.fabric_extras.ranged_weapon.api.CustomBow;
import net.fabric_extras.ranged_weapon.api.RangedConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class TestMod implements ModInitializer {
    public static final String NAMESPACE = "testmod";
    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {

        var bow = new CustomBow(
                new FabricItemSettings().maxDamage(300),
                () -> Ingredient.ofItems(Items.GOLD_INGOT)
        );
        bow.config(new RangedConfig(20, 8, null));

        Registry.register(
                Registries.ITEM,
                new Identifier(NAMESPACE, "bow"),
                bow
        );
    }
}