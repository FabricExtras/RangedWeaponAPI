package net.testmod;

import net.fabric_extras.ranged_weapon.api.CustomBow;
import net.fabric_extras.ranged_weapon.api.RangedWeaponConfig;
import net.fabricmc.api.ModInitializer;
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
                new Item.Settings().maxDamage(300),
                new RangedWeaponConfig(9, 40, 1F, null),
                () -> Ingredient.ofItems(Items.GOLD_INGOT)
        );
        Registry.register(
                Registries.ITEM,
                Identifier.of(NAMESPACE, "custom_longbow"),
                bow
        );
    }
}
