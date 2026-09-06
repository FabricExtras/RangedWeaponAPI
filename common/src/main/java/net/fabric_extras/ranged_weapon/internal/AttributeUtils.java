package net.fabric_extras.ranged_weapon.internal;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.fabric_extras.ranged_weapon.api.AttributeModifierIDs;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabric_extras.ranged_weapon.api.RangedConfig;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/// 1.20.1 replacement for 2.3.4's `AttributeModifiersComponent` plumbing: there are no data components
/// here, so a `RangedConfig` becomes a Guava `Multimap` returned from `Item#getAttributeModifiers(EquipmentSlot)`.
public class AttributeUtils {

    public static Multimap<EntityAttribute, EntityAttributeModifier> fromRangedConfig(RangedConfig config) {
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();

        builder.put(EntityAttributes_RangedWeapon.DAMAGE.attribute, new EntityAttributeModifier(
                AttributeModifierIDs.WEAPON_DAMAGE_UUID,
                AttributeModifierIDs.WEAPON_DAMAGE_ID.toString(),
                config.damage(),
                EntityAttributeModifier.Operation.ADDITION));
        builder.put(EntityAttributes_RangedWeapon.PULL_TIME.attribute, new EntityAttributeModifier(
                AttributeModifierIDs.WEAPON_PULL_TIME_UUID,
                AttributeModifierIDs.WEAPON_PULL_TIME_ID.toString(),
                config.pull_time_bonus(),
                EntityAttributeModifier.Operation.ADDITION));
        if (config.velocity_bonus() > 0) {
            builder.put(EntityAttributes_RangedWeapon.VELOCITY.attribute, new EntityAttributeModifier(
                    AttributeModifierIDs.WEAPON_VELOCITY_UUID,
                    AttributeModifierIDs.WEAPON_VELOCITY_ID.toString(),
                    config.velocity_bonus(),
                    EntityAttributeModifier.Operation.ADDITION));
        }

        addExtraAttributes(builder, config);

        return builder.build();
    }

    private static void addExtraAttributes(ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder, RangedConfig config) {
        var attributes = config.attributes();
        if (attributes == null || attributes.isEmpty()) {
            return;
        }
        for (var attr : attributes) {
            if (attr == null || attr.modifier() == null) { continue; }
            Identifier entityAttributeId = Identifier.tryParse(attr.attributeId());
            if (entityAttributeId == null) { continue; }
            var entityAttribute = Registries.ATTRIBUTE.get(entityAttributeId);
            if (entityAttribute == null) { continue; }
            Identifier modifierId = Identifier.tryParse(attr.modifier().modifierId());
            if (modifierId == null) { continue; }
            builder.put(entityAttribute, new EntityAttributeModifier(
                    AttributeModifierIDs.uuid(modifierId),
                    modifierId.toString(),
                    attr.modifier().value(),
                    attr.modifier().operation()
            ));
        }
    }
}
