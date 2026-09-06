package net.fabric_extras.ranged_weapon.internal;

import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;

public class ScalingUtil {

    public static final float STANDARD_BOW_VELOCITY = 3F;
    public static final float STANDARD_BOW_DAMAGE = 6.0F;
    public static final float STANDARD_CROSSBOW_VELOCITY = 3.15F;
    public static final float STANDARD_CROSSBOW_DAMAGE = 9.0F;

    public static final Scaling BOW_BASELINE = new Scaling(STANDARD_BOW_VELOCITY, STANDARD_BOW_DAMAGE);
    public static final Scaling CROSSBOW_BASELINE = new Scaling(STANDARD_CROSSBOW_VELOCITY, STANDARD_CROSSBOW_DAMAGE);

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

    public static double arrowVelocityMultiplier(double standardVelocity, double customVelocity) {
        return customVelocity / standardVelocity;
    }

    public static double arrowVelocityMultiplier(Item item, double bonusVelocity) {
        var baseline = baselineFor(item);
        return (baseline.velocity() + bonusVelocity) / baseline.velocity();
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
