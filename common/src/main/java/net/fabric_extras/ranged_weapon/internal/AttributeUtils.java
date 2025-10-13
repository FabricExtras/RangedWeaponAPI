package net.fabric_extras.ranged_weapon.internal;

import net.fabric_extras.ranged_weapon.api.AttributeModifierIDs;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabric_extras.ranged_weapon.api.RangedConfig;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class AttributeUtils {
    public static AttributeModifiersComponent mergeComponents(AttributeModifiersComponent target, AttributeModifiersComponent source) {
        if (source == null && target == null) {
            return null;
        } else if (source == null) {
            return target;
        } else if (target == null) {
            return source;
        }
        var builder =  AttributeModifiersComponent.builder();
        for (var entry: source.modifiers()) {
            builder.add(entry.attribute(), entry.modifier(), entry.slot());
        }
        for (var entry: target.modifiers()) {
            builder.add(entry.attribute(), entry.modifier(), entry.slot());
        }
        return builder.build();
    }

    // Matching the interface of `add(...)` in AttributeModifiersComponent.Builder
    public record ComponentEntry(RegistryEntry<EntityAttribute> attribute, EntityAttributeModifier modifier, AttributeModifierSlot slot) { }

    public static AttributeModifiersComponent fromRangedConfig(RangedConfig config) {
        var slot = AttributeModifierSlot.HAND;

        var damage = new EntityAttributeModifier(
                AttributeModifierIDs.WEAPON_DAMAGE_ID,
                config.damage(),
                EntityAttributeModifier.Operation.ADD_VALUE);
        var pullTime = new EntityAttributeModifier(
                AttributeModifierIDs.WEAPON_PULL_TIME_ID,
                config.pull_time_bonus(),
                EntityAttributeModifier.Operation.ADD_VALUE);
        var builder = AttributeModifiersComponent.builder()
                .add(EntityAttributes_RangedWeapon.DAMAGE.entry, damage, slot)
                .add(EntityAttributes_RangedWeapon.PULL_TIME.entry, pullTime, slot);
        if (config.velocity_bonus() > 0) {
            var velocity = new EntityAttributeModifier(
                    AttributeModifierIDs.WEAPON_VELOCITY_ID,
                    config.velocity_bonus(),
                    EntityAttributeModifier.Operation.ADD_VALUE);
            builder.add(EntityAttributes_RangedWeapon.VELOCITY.entry, velocity, slot);
        }

        for (var entry: componentEntriesFrom(config.attributes(), slot)) {
            builder.add(entry.attribute, entry.modifier, entry.slot);
        }

        return builder.build();
    }


    public static List<ComponentEntry> componentEntriesFrom(List<RangedConfig.Attribute> attributes, AttributeModifierSlot slot) {
        var list = new ArrayList<ComponentEntry>();
        if (attributes == null || attributes.isEmpty()) {
            return list;
        }
        for (var attr: attributes) {
            Identifier entityAttributeId;
            try {
                entityAttributeId = Identifier.tryParse(attr.attributeId());
            } catch (Exception e) {
                continue;
            }
            var entityAttribute = Registries.ATTRIBUTE.getEntry(entityAttributeId);
            if (entityAttribute.isEmpty() || attr.modifier() == null) { continue; }
            Identifier modifierId;
            try {
                modifierId = Identifier.tryParse(attr.modifier().modifierId());
            } catch (Exception e) {
                continue;
            }
            list.add(new ComponentEntry(
                    entityAttribute.get(),
                    new EntityAttributeModifier(
                            modifierId,
                            attr.modifier().value(),
                            attr.modifier().operation()
                    ),
                    slot
            ));
        }
        return list;
    }
}
