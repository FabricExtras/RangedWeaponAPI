package net.testmod;

import net.fabric_extras.ranged_weapon.api.CustomBow;
import net.fabric_extras.ranged_weapon.api.RangedConfig;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod("testmod")
public class TestMod {
    public static final String NAMESPACE = "testmod";

    public TestMod(IEventBus eventBus) {
        // Initialize the test mod
        var bow = new CustomBow(
                new Item.Settings().maxDamage(300),
                new RangedConfig(9, 1F, 1),
                () -> Ingredient.ofItems(Items.GOLD_INGOT)
        );

        Registry.register(
                Registries.ITEM,
                Identifier.of(NAMESPACE, "custom_longbow"),
                bow
        );
    }
}