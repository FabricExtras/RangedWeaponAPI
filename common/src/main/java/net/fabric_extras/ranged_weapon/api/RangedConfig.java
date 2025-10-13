package net.fabric_extras.ranged_weapon.api;

import net.fabric_extras.ranged_weapon.internal.ScalingUtil;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the configurable properties of ranged weapons.
 * @param pull_time_bonus - the time (in seconds) added to standard pull time (1 sec)
 * @param damage - the amount of damage the weapon deals
 * @param velocity_bonus - added speed to the projectile, Does not affect the projectile damage!
 */
public record RangedConfig(float damage, float pull_time_bonus, float velocity_bonus, @Nullable List<Attribute> attributes) {
    public static final RangedConfig EMPTY = new RangedConfig(0, 0, 0);
    public static final RangedConfig BOW = new RangedConfig((float) ScalingUtil.BOW_BASELINE.damage(), 0, 0);
    public static final RangedConfig CROSSBOW = new RangedConfig( (float) ScalingUtil.CROSSBOW_BASELINE.damage(), 0.25F, 0);

    public RangedConfig(float damage, float pull_time_bonus, float velocity_bonus) {
        this(damage, pull_time_bonus, velocity_bonus, null);
    }

    public record Attribute(String attributeId, Modifier modifier) { }
    public record Modifier(String modifierId, EntityAttributeModifier.Operation operation, double value) {  }
    public RangedConfig withAttributes(@Nullable List<Attribute> attributes) {
        return new RangedConfig(damage, pull_time_bonus, velocity_bonus, attributes);
    }
    public RangedConfig withAttribute(Identifier attributeId, Identifier modifierId, EntityAttributeModifier.Operation operation, double value) {
        var list = new ArrayList<>(attributes != null ? attributes : List.of());
        var newEntry = new Attribute(attributeId.toString(), new Modifier(modifierId.toString(), operation, value));
        list.add(newEntry);
        return new RangedConfig(damage, pull_time_bonus, velocity_bonus, list);
    }
    public RangedConfig withAttribute(Identifier attributeId, EntityAttributeModifier.Operation operation, double value) {
        return withAttribute(attributeId, AttributeModifierIDs.OTHER_BONUS_ID, operation, value);
    }
}
