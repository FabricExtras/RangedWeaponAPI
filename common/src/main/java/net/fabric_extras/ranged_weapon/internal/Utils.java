package net.fabric_extras.ranged_weapon.internal;

import net.minecraft.component.type.AttributeModifiersComponent;

public class Utils {
    public static AttributeModifiersComponent mergeAttributeComponents(AttributeModifiersComponent target, AttributeModifiersComponent source) {
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
}
