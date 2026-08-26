package net.rpg_foundation.ranged_weapon.api;

import net.minecraft.world.item.BowItem;
import net.rpg_foundation.ranged_weapon.internal.AttributeUtils;
import java.util.HashSet;

public class CustomBow extends BowItem {
    // Instances are kept a list of, so model predicates can be automatically registered
    public final static HashSet<CustomBow> instances = new HashSet<>();
    public CustomBow(Properties settings, RangedWeaponConfig config) {
        super(
                AttributeUtils.configure(settings, config)
        );
        instances.add(this);
    }

    /**
     * @deprecated Use the {@link RangedWeaponConfig} constructor instead
     */
    @Deprecated
    public CustomBow(Properties settings, RangedConfig config) {
        this(settings, config.toAbsolute());
    }
}

