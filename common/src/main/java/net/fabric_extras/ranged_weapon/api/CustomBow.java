package net.fabric_extras.ranged_weapon.api;

import net.fabric_extras.ranged_weapon.internal.RangedItemSettings;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

import java.util.HashSet;
import java.util.function.Supplier;

public class CustomBow extends BowItem {
    // Instances are kept a list of, so model predicates can be automatically registered
    public final static HashSet<CustomBow> instances = new HashSet<>();
    public CustomBow(Settings settings, RangedConfig config, Supplier<Ingredient> repairIngredientSupplier) {
        super(
                ((RangedItemSettings)settings).rangedAttributes(config)
        );
        this.repairIngredientSupplier = repairIngredientSupplier;
        instances.add(this);
    }

    private final Supplier<Ingredient> repairIngredientSupplier;

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return this.repairIngredientSupplier.get().test(ingredient) || super.canRepair(stack, ingredient);
    }
}

