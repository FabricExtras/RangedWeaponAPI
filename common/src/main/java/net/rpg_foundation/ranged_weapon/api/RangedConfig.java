package net.rpg_foundation.ranged_weapon.api;

import net.rpg_foundation.ranged_weapon.internal.ScalingUtil;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated Use {@link RangedWeaponConfig} instead, which uses absolute values.
 * This type is kept temporarily for API and config file compatibility, and will be deleted.
 *
 * @param damage - the amount of damage the weapon deals at full charge (with a standard arrow)
 * @param pull_time_bonus - the time (in seconds) added to standard pull time (1 sec)
 * @param velocity_bonus - added speed to the projectile, Does not affect the projectile damage!
 */
@Deprecated
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

    /**
     * Converts to the replacement type, translating bonus values to absolute values.
     */
    public RangedWeaponConfig toAbsolute() {
        List<RangedWeaponConfig.Attribute> converted = null;
        if (attributes != null) {
            converted = attributes.stream()
                    .map(entry -> new RangedWeaponConfig.Attribute(
                            entry.attributeId(),
                            new RangedWeaponConfig.Modifier(
                                    entry.modifier().modifierId(),
                                    entry.modifier().operation(),
                                    entry.modifier().value())))
                    .toList();
        }
        return new RangedWeaponConfig(
                damage,
                (1.0F + pull_time_bonus) * 20F,
                velocity_bonus > 0 ? velocity_bonus : null,
                converted);
    }
}
