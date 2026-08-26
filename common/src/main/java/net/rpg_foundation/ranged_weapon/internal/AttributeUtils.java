package net.rpg_foundation.ranged_weapon.internal;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.rpg_foundation.ranged_weapon.api.AttributeModifierIDs;
import net.rpg_foundation.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponConfig;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponProperties;
import net.rpg_foundation.ranged_weapon.mixin.item.ComponentMapBuilderAccessor;
import net.rpg_foundation.ranged_weapon.mixin.item.ItemSettingsAccessor;
import java.util.ArrayList;
import java.util.List;

public class AttributeUtils {

    /**
     * Applies the given config to the item settings, by attaching:
     * - the `ranged_weapon:properties` component (carrying the pull time)
     * - the attribute modifiers derived from the config (merged with any already present)
     */
    public static Item.Properties configure(Item.Properties settings, RangedWeaponConfig config) {
        var generatedAttributes = fromConfig(config);
        var applicableAttributes = mergeComponents(generatedAttributes, existingAttributes(settings));
        settings.attributes(applicableAttributes);
        settings.component(RangedWeaponProperties.TYPE, new RangedWeaponProperties(config.pullTimeTicks()));
        return settings;
    }

    /**
     * Whether the settings already carry a `ranged_weapon:properties` component.
     */
    public static boolean hasProperties(Item.Properties settings) {
        var componentBuilder = ((ItemSettingsAccessor) settings).rwa_getComponents();
        if (componentBuilder == null) {
            return false;
        }
        var components = ((ComponentMapBuilderAccessor) componentBuilder).rwa_components();
        return components.get(RangedWeaponProperties.TYPE) instanceof RangedWeaponProperties;
    }

    private static ItemAttributeModifiers existingAttributes(Item.Properties settings) {
        var componentBuilder = ((ItemSettingsAccessor) settings).rwa_getComponents();
        if (componentBuilder != null) {
            var existingComponents = ((ComponentMapBuilderAccessor) componentBuilder).rwa_components();
            var existing = existingComponents.get(DataComponents.ATTRIBUTE_MODIFIERS);
            if (existing instanceof ItemAttributeModifiers attributeModifiers) {
                return attributeModifiers;
            }
        }
        return null;
    }

    public static ItemAttributeModifiers mergeComponents(ItemAttributeModifiers target, ItemAttributeModifiers source) {
        if (source == null && target == null) {
            return null;
        } else if (source == null) {
            return target;
        } else if (target == null) {
            return source;
        }
        var builder =  ItemAttributeModifiers.builder();
        for (var entry: source.modifiers()) {
            builder.add(entry.attribute(), entry.modifier(), entry.slot());
        }
        for (var entry: target.modifiers()) {
            builder.add(entry.attribute(), entry.modifier(), entry.slot());
        }
        return builder.build();
    }

    // Matching the interface of `add(...)` in AttributeModifiersComponent.Builder
    public record ComponentEntry(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) { }

    public static ItemAttributeModifiers fromConfig(RangedWeaponConfig config) {
        var slot = EquipmentSlotGroup.HAND;

        var damage = new AttributeModifier(
                AttributeModifierIDs.WEAPON_DAMAGE_ID,
                config.damage(),
                AttributeModifier.Operation.ADD_VALUE);
        // The pull time modifier is display-only, actual pull time is driven by `RangedWeaponProperties`.
        // Value is the bonus over the attribute base (1 sec), tooltip rendering adds the base back.
        var pullTime = new AttributeModifier(
                AttributeModifierIDs.WEAPON_PULL_TIME_ID,
                (config.pull_time() / 20F) - 1.0F,
                AttributeModifier.Operation.ADD_VALUE);
        var builder = ItemAttributeModifiers.builder()
                .add(EntityAttributes_RangedWeapon.DAMAGE.entry, damage, slot)
                .add(EntityAttributes_RangedWeapon.PULL_TIME.entry, pullTime, slot);
        if (config.velocity() != null && config.velocity() != 0) {
            var velocity = new AttributeModifier(
                    AttributeModifierIDs.WEAPON_VELOCITY_ID,
                    config.velocity(),
                    AttributeModifier.Operation.ADD_VALUE);
            builder.add(EntityAttributes_RangedWeapon.VELOCITY.entry, velocity, slot);
        }

        for (var entry: componentEntriesFrom(config.attributes(), slot)) {
            builder.add(entry.attribute, entry.modifier, entry.slot);
        }

        return builder.build();
    }


    public static List<ComponentEntry> componentEntriesFrom(List<RangedWeaponConfig.Attribute> attributes, EquipmentSlotGroup slot) {
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
            var entityAttribute = BuiltInRegistries.ATTRIBUTE.get(entityAttributeId);
            if (entityAttribute.isEmpty() || attr.modifier() == null) { continue; }
            Identifier modifierId;
            try {
                modifierId = Identifier.tryParse(attr.modifier().modifierId());
            } catch (Exception e) {
                continue;
            }
            list.add(new ComponentEntry(
                    entityAttribute.get(),
                    new AttributeModifier(
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
