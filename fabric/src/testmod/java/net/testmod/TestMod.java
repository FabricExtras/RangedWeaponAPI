package net.testmod;

import net.fabric_extras.ranged_weapon.api.CustomBow;
import net.fabric_extras.ranged_weapon.api.CustomCrossbow;
import net.fabric_extras.ranged_weapon.api.RangedConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/// Dev-only probe mod for the RangedWeaponAPI hooks. Never shipped.
///
/// 1.20.1 delta: registration does **not** happen in `onInitialize`. RangedWeaponAPI has no Fabric API
/// dependency, so nothing defers `Bootstrap.initialize()`'s registry freeze; by the time `main`
/// entrypoints run, `Registries.ITEM` is frozen and merely *constructing* an `Item` throws
/// "This registry can't create intrusive holders". So the items are built from an `Items.<clinit>`
/// TAIL mixin instead — the same window-based idiom `RangedWeaponMod` uses for attributes and status
/// effects on this branch.
public class TestMod implements ModInitializer {
    public static final String NAMESPACE = "testmod";

    private static boolean registered = false;

    /// Called from `net.testmod.mixin.ItemsMixin` at `Items.<clinit>` TAIL. Idempotent.
    public static void registerItems() {
        if (registered) {
            return;
        }
        registered = true;

        // Bow probe: 40 tick pull (vanilla is 20), damage 9 against the 6 bow baseline.
        var bow = new CustomBow(
                new Item.Settings().maxDamage(300),
                new RangedConfig(9, 1F, 1),
                () -> Ingredient.ofItems(Items.GOLD_INGOT)
        );
        Registry.register(
                Registries.ITEM,
                new Identifier(NAMESPACE, "custom_longbow"),
                bow
        );

        // Crossbow probe: pillagers/piglins holding this must charge AND actually fire.
        // 35 tick pull (vanilla crossbow is 25), damage 12 against the 9 crossbow baseline.
        var crossbow = new CustomCrossbow(
                new Item.Settings().maxDamage(465),
                new RangedConfig(12, 0.75F, 0),
                () -> Ingredient.ofItems(Items.GOLD_INGOT)
        );
        Registry.register(
                Registries.ITEM,
                new Identifier(NAMESPACE, "custom_crossbow"),
                crossbow
        );
    }

    @Override
    public void onInitialize() {
        // Registration already happened during `Items.<clinit>`; this only reports it.
        registerItems();
    }
}
