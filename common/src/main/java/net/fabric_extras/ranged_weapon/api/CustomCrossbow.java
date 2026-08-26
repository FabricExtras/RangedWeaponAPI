package net.fabric_extras.ranged_weapon.api;

import net.fabric_extras.ranged_weapon.internal.AttributeUtils;
import net.minecraft.item.CrossbowItem;

import java.util.HashSet;

public class CustomCrossbow extends CrossbowItem {
    // Instances are kept a list of, so model predicates can be automatically registered
    public final static HashSet<CustomCrossbow> instances = new HashSet<>();

    public CustomCrossbow(Settings settings, RangedWeaponConfig config) {
        super(
                AttributeUtils.configure(settings, config)
        );
        instances.add(this);
    }

    /**
     * @deprecated Use the {@link RangedWeaponConfig} constructor instead
     */
    @Deprecated
    public CustomCrossbow(Settings settings, RangedConfig config) {
        this(settings, config.toAbsolute());
    }
}
