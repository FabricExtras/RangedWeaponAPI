package net.fabric_extras.ranged_weapon.api;

import net.fabric_extras.ranged_weapon.internal.AttributeUtils;
import net.minecraft.item.CrossbowItem;
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

    /**
     * Repairing is resolved by `ItemStack.canRepairWith` (see `mixin.item.ItemStackRepairMixin`):
     * the supplied ingredient is accepted on top of the `minecraft:repairable` component (if any).
     */
    public Supplier<Ingredient> getRepairIngredientSupplier() {
        return repairIngredientSupplier;
    }
}
