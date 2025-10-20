package net.fabric_extras.ranged_weapon.internal;

import net.fabric_extras.ranged_weapon.api.component.RangedWeaponComponents;
import net.fabric_extras.ranged_weapon.api.component.RangedWeaponProperties;
import net.minecraft.item.Item;

public class ScalingUtil {
    public static RangedWeaponProperties baselineFor(Item item) {
        var data = item.getComponents().get(RangedWeaponComponents.BASELINE);
        return data != null ? data : RangedWeaponProperties.EMPTY;
    }

    public static double arrowVelocityMultiplier(RangedWeaponProperties baseline, double bonusVelocity) {
        return (baseline.arrow_velocity() + bonusVelocity) / baseline.arrow_velocity();
    }

    public static double arrowDamageMultiplier(RangedWeaponProperties baseline, double attributeDamage, double velocityMultiplier) {
        // Boost damage based on the attribute
        var multiplier = (attributeDamage / baseline.damage());
        if (velocityMultiplier != 1) {
            // Counteract the damage boost by caused by non-standard velocity
            multiplier /= velocityMultiplier;
        }
        return multiplier;
    }
}
