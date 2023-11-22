package net.fabric_extras.ranged_weapon.api;

import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

import java.util.HashSet;
import java.util.function.Supplier;

public class CustomCrossbow extends CrossbowItem {
    // Instances are kept a list of, so model predicates can be automatically registered
    public final static HashSet<CustomCrossbow> instances = new HashSet<>();

    public CustomCrossbow(Settings settings, Supplier<Ingredient> repairIngredientSupplier) {
        super(settings);
        this.repairIngredientSupplier = repairIngredientSupplier;
        instances.add(this);
    }

    public void config(RangedConfig config) {
        ((CustomRangedWeaponProperties) this).configure(config);
    }

    private final Supplier<Ingredient> repairIngredientSupplier;

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return this.repairIngredientSupplier.get().test(ingredient) || super.canRepair(stack, ingredient);
    }
}
