package net.rpg_foundation.ranged_weapon.internal;

import net.rpg_foundation.ranged_weapon.api.AttributeModifierIDs;
import net.rpg_foundation.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponConfig;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponProperties;
import net.rpg_foundation.ranged_weapon.mixin.item.ComponentMapBuilderAccessor;
import net.rpg_foundation.ranged_weapon.mixin.item.ItemSettingsAccessor;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class AttributeUtils {

    /**
     * Applies the given config to the item settings, by attaching:
     * - the `ranged_weapon:properties` component (carrying the pull time)
     * - the attribute modifiers derived from the config (merged with any already present)
     */
    public static Item.Settings configure(Item.Settings settings, RangedWeaponConfig config) {
        var generatedAttributes = fromConfig(config);
        var applicableAttributes = mergeComponents(generatedAttributes, existingAttributes(settings));
        settings.attributeModifiers(applicableAttributes);
        settings.component(RangedWeaponProperties.TYPE, new RangedWeaponProperties(config.pullTimeTicks()));
        return settings;
    }

    /**
     * Whether the settings already carry a `ranged_weapon:properties` component.
     */
    public static boolean hasProperties(Item.Settings settings) {
        var componentBuilder = ((ItemSettingsAccessor) settings).rwa_getComponents();
        if (componentBuilder == null) {
            return false;
        }
        var components = ((ComponentMapBuilderAccessor) componentBuilder).rwa_components();
        return components.get(RangedWeaponProperties.TYPE) instanceof RangedWeaponProperties;
    }

    private static AttributeModifiersComponent existingAttributes(Item.Settings settings) {
        var componentBuilder = ((ItemSettingsAccessor) settings).rwa_getComponents();
        if (componentBuilder != null) {
            var existingComponents = ((ComponentMapBuilderAccessor) componentBuilder).rwa_components();
            var existing = existingComponents.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            if (existing instanceof AttributeModifiersComponent attributeModifiers) {
                return attributeModifiers;
            }
        }
        return null;
    }

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

    public static AttributeModifiersComponent fromConfig(RangedWeaponConfig config) {
        var slot = AttributeModifierSlot.HAND;

        var damage = new EntityAttributeModifier(
                AttributeModifierIDs.WEAPON_DAMAGE_ID,
                config.damage(),
                EntityAttributeModifier.Operation.ADD_VALUE);
        // The pull time modifier is display-only, actual pull time is driven by `RangedWeaponProperties`.
        // Value is the bonus over the attribute base (1 sec), tooltip rendering adds the base back.
        var pullTime = new EntityAttributeModifier(
                AttributeModifierIDs.WEAPON_PULL_TIME_ID,
                (config.pull_time() / 20F) - 1.0F,
                EntityAttributeModifier.Operation.ADD_VALUE);
        var builder = AttributeModifiersComponent.builder()
                .add(EntityAttributes_RangedWeapon.DAMAGE.entry, damage, slot)
                .add(EntityAttributes_RangedWeapon.PULL_TIME.entry, pullTime, slot);
        if (config.velocity() != null && config.velocity() != 0) {
            var velocity = new EntityAttributeModifier(
                    AttributeModifierIDs.WEAPON_VELOCITY_ID,
                    config.velocity(),
                    EntityAttributeModifier.Operation.ADD_VALUE);
            builder.add(EntityAttributes_RangedWeapon.VELOCITY.entry, velocity, slot);
        }

        for (var entry: componentEntriesFrom(config.attributes(), slot)) {
            builder.add(entry.attribute, entry.modifier, entry.slot);
        }

        return builder.build();
    }


    public static List<ComponentEntry> componentEntriesFrom(List<RangedWeaponConfig.Attribute> attributes, AttributeModifierSlot slot) {
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
