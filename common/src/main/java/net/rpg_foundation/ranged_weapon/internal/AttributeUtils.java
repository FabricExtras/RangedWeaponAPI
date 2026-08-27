package net.rpg_foundation.ranged_weapon.internal;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
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
import org.jspecify.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public class AttributeUtils {

    /**
     * Applies the given config to the item settings.
     * Since 26.1 item components are bound to the registry holder during resource reload
     * (`DataComponentInitializers`), so the config is translated into a delayed initializer step,
     * appended to the settings' initializer chain, attaching:
     * - the `ranged_weapon:properties` component (carrying the pull time)
     * - the attribute modifiers derived from the config (merged with any set by earlier steps,
     *   e.g. `Item.Properties#attributes`)
     * Attribute ids listed in the config are resolved at reload time, not at item construction.
     */
    public static Item.Properties configure(Item.Properties settings, RangedWeaponConfig config) {
        append(settings, (components, context, key) -> apply(components, config));
        return settings;
    }

    /**
     * Like {@link #configure}, but the step only applies the config if no earlier step attached a
     * `ranged_weapon:properties` component. Used to give vanilla bows/crossbows (and third-party
     * subclasses) their defaults without overriding an explicit configuration.
     */
    public static Item.Properties configureDefaults(Item.Properties settings, RangedWeaponConfig config) {
        append(settings, (components, context, key) -> {
            if (!hasProperties(components)) {
                apply(components, config);
            }
        });
        return settings;
    }

    /**
     * Whether the component builder already carries a `ranged_weapon:properties` component.
     */
    public static boolean hasProperties(DataComponentMap.Builder components) {
        return componentsOf(components).get(RangedWeaponProperties.TYPE) instanceof RangedWeaponProperties;
    }

    private static void append(Item.Properties settings, DataComponentInitializers.Initializer<Item> step) {
        var accessor = (ItemSettingsAccessor) settings;
        accessor.rwa_setComponentInitializer(accessor.rwa_getComponentInitializer().andThen(step));
    }

    private static void apply(DataComponentMap.Builder components, RangedWeaponConfig config) {
        var applicableAttributes = mergeComponents(fromConfig(config), existingAttributes(components));
        components.set(DataComponents.ATTRIBUTE_MODIFIERS, applicableAttributes);
        components.set(RangedWeaponProperties.TYPE, new RangedWeaponProperties(config.pullTimeTicks()));
    }

    private static Reference2ObjectMap<DataComponentType<?>, Object> componentsOf(DataComponentMap.Builder components) {
        return ((ComponentMapBuilderAccessor) components).rwa_components();
    }

    private static @Nullable ItemAttributeModifiers existingAttributes(DataComponentMap.Builder components) {
        var existing = componentsOf(components).get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (existing instanceof ItemAttributeModifiers attributeModifiers) {
            return attributeModifiers;
        }
        return null;
    }

    public static @Nullable ItemAttributeModifiers mergeComponents(@Nullable ItemAttributeModifiers target, @Nullable ItemAttributeModifiers source) {
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
