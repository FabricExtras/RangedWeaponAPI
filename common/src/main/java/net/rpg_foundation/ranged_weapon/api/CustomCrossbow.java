package net.rpg_foundation.ranged_weapon.api;

import net.minecraft.world.item.CrossbowItem;
import net.rpg_foundation.ranged_weapon.internal.AttributeUtils;
import java.util.HashSet;

public class CustomCrossbow extends CrossbowItem {
    // Instances are kept a list of, so model predicates can be automatically registered
    public final static HashSet<CustomCrossbow> instances = new HashSet<>();

    public CustomCrossbow(Properties settings, RangedWeaponConfig config) {
        super(
                AttributeUtils.configure(settings, config)
        );
        instances.add(this);
    }

    /**
     * @deprecated Use the {@link RangedWeaponConfig} constructor instead
     */
    @Deprecated
    public CustomCrossbow(Properties settings, RangedConfig config) {
        this(settings, config.toAbsolute());
    }
}
