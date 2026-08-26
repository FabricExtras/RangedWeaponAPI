package net.rpg_foundation.ranged_weapon.api;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Authoring/config surface for ranged weapons, using absolute values.
 * Applied to items by being translated into:
 * - the `ranged_weapon:properties` component ({@link RangedWeaponProperties}), carrying the pull time
 * - plain attribute modifiers (`ranged_weapon:damage`, display-only `ranged_weapon:pull_time`, optionally `ranged_weapon:velocity`)
 *
 * @param damage     the damage the weapon deals at full charge (with a standard arrow),
 *                   becomes a `ranged_weapon:damage` attribute modifier
 * @param pull_time  the pull time of the weapon, in ticks (20 = vanilla bow, 25 = vanilla crossbow)
 * @param velocity   OPTIONAL. Bonus projectile speed, becomes a `ranged_weapon:velocity` attribute modifier.
 *                   Does not affect the projectile damage!
 * @param attributes OPTIONAL. Additional attribute modifiers to attach to the weapon
 */
public record RangedWeaponConfig(float damage, float pull_time, @Nullable Float velocity, @Nullable List<Attribute> attributes) {
    public static final RangedWeaponConfig BOW = new RangedWeaponConfig(6, 20);
    public static final RangedWeaponConfig CROSSBOW = new RangedWeaponConfig(9, 25);

    public RangedWeaponConfig(float damage, float pull_time) {
        this(damage, pull_time, null, null);
    }

    public record Attribute(String attributeId, Modifier modifier) { }
    public record Modifier(String modifierId, AttributeModifier.Operation operation, double value) { }

    public RangedWeaponConfig withAttributes(@Nullable List<Attribute> attributes) {
        return new RangedWeaponConfig(damage, pull_time, velocity, attributes);
    }

    public RangedWeaponConfig withAttribute(Identifier attributeId, Identifier modifierId, AttributeModifier.Operation operation, double value) {
        var list = new ArrayList<>(attributes != null ? attributes : List.of());
        list.add(new Attribute(attributeId.toString(), new Modifier(modifierId.toString(), operation, value)));
        return new RangedWeaponConfig(damage, pull_time, velocity, list);
    }

    public RangedWeaponConfig withAttribute(Identifier attributeId, AttributeModifier.Operation operation, double value) {
        return withAttribute(attributeId, AttributeModifierIDs.OTHER_BONUS_ID, operation, value);
    }

    public int pullTimeTicks() {
        return Math.round(pull_time);
    }
}
