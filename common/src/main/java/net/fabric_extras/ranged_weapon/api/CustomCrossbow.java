package net.fabric_extras.ranged_weapon.api;

import net.fabric_extras.ranged_weapon.internal.AttributeUtils;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

import java.util.HashSet;
import java.util.function.Supplier;

public class CustomCrossbow extends CrossbowItem {
    // Instances are kept a list of, so model predicates can be automatically registered
    public final static HashSet<CustomCrossbow> instances = new HashSet<>();

    public CustomCrossbow(Settings settings, RangedWeaponConfig config, Supplier<Ingredient> repairIngredientSupplier) {
        super(
                AttributeUtils.configure(settings, config)
        );
        this.repairIngredientSupplier = repairIngredientSupplier;
        instances.add(this);
    }

    /**
     * @deprecated Use the {@link RangedWeaponConfig} constructor instead
     */
    @Deprecated
    public CustomCrossbow(Settings settings, RangedConfig config, Supplier<Ingredient> repairIngredientSupplier) {
        this(settings, config.toAbsolute(), repairIngredientSupplier);
    }

    private final Supplier<Ingredient> repairIngredientSupplier;

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return this.repairIngredientSupplier.get().test(ingredient) || super.canRepair(stack, ingredient);
    }

    public Supplier<Ingredient> getRepairIngredientSupplier() {
        return repairIngredientSupplier;
    }
}
