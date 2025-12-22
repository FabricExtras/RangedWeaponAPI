package net.fabric_extras.ranged_weapon.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.recipe.EmiAnvilRecipe;
import net.fabric_extras.ranged_weapon.api.CustomBow;
import net.fabric_extras.ranged_weapon.api.CustomCrossbow;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

@EmiEntrypoint
public class RangedWeaponEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
        // Register anvil repair recipes for CustomBow instances
        for (CustomBow bow : CustomBow.instances) {
            registerAnvilRecipe(registry, bow);
        }

        // Register anvil repair recipes for CustomCrossbow instances
        for (CustomCrossbow crossbow : CustomCrossbow.instances) {
            registerAnvilRecipe(registry, crossbow);
        }
    }

    private void registerAnvilRecipe(EmiRegistry registry, Item item) {
        // Get the repair ingredient
        EmiIngredient repairMaterial;

        if (item instanceof CustomBow bow) {
            repairMaterial = EmiIngredient.of(bow.getRepairTag());
        } else if (item instanceof CustomCrossbow crossbow) {
            repairMaterial = EmiIngredient.of(crossbow.getRepairTag());
        } else {
            return;
        }

        var itemEntry = Registries.ITEM.getEntry(item);
        if (itemEntry == null || itemEntry.getKey().isEmpty()) {
            return; // Item is not registered, cannot create recipe
        }
        var itemId = itemEntry.getKey().get().getValue();

        // Create the anvil recipe
        Identifier id = Identifier.of(itemId.getNamespace(), "anvil_repair_rwa/" +
                itemId.getPath());

        registry.addRecipe(new EmiAnvilRecipe(EmiStack.of(item), repairMaterial, id));
    }
}
