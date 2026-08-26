package net.rpg_foundation.ranged_weapon.internal;

import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;

public class ScalingUtil {

    // Velocities mirror vanilla launch speeds (BowItem full charge, CrossbowItem arrows),
    // damage = arrow base damage (2) × launch velocity
    public static final Scaling BOW_BASELINE = new Scaling(3.0, 6.0);
    public static final Scaling CROSSBOW_BASELINE = new Scaling(3.15, 9.0);

    public record Scaling(double velocity, double damage) { }

    public static Scaling baselineFor(Item item) {
        if (item instanceof BowItem) {
            return BOW_BASELINE;
        } else if (item instanceof CrossbowItem) {
            return CROSSBOW_BASELINE;
        } else {
            return new Scaling(1, 1);
        }
    }

    public static double arrowDamageMultiplier(double standardDamage, double attributeDamage, double velocityMultiplier) {
        // Boost damage based on the attribute
        var multiplier = (attributeDamage / standardDamage);
        if (velocityMultiplier != 1) {
            // Counteract the damage boost by caused by non-standard velocity
            multiplier /= velocityMultiplier;
        }
        return multiplier;
    }
}
