package net.rpg_foundation.ranged_weapon.api;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.rpg_foundation.ranged_weapon.Platform;
import net.rpg_foundation.ranged_weapon.internal.NeoAttribute;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class EntityAttributes_RangedWeapon {
    public static final String NAMESPACE = "ranged_weapon";
    public static final ArrayList<Entry> all = new ArrayList<>();
    private static Entry entry(String name, double baseValue, boolean tracked) {
        return entry(name, 0, baseValue, tracked);
    }
    private static Entry entry(String name, double minValue, double baseValue, boolean tracked) {
        var entry = new Entry(name, minValue, baseValue, tracked);
        all.add(entry);
        return entry;
    }

    public static class Entry {
        public final Identifier id;
        public final String translationKey;
        public final Attribute attribute;
        public final double baseValue;
        @Nullable public Holder<Attribute> entry;

        public Entry(String name, double minValue, double baseValue, boolean tracked) {
            this.id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
            this.translationKey = "attribute.name." + NAMESPACE + "." + name;
            this.attribute = Platform.util().makeAttribute(translationKey, baseValue, minValue, 2048).setSyncable(tracked);
            this.baseValue = baseValue;
        }

        public double asMultiplier(double attributeValue) {
            return attributeValue / baseValue;
        }

        public void register() {
            entry = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, id, attribute);
        }

        public Entry setBaseAttributeId(Identifier id) {
            if (attribute instanceof NeoAttribute neo) {
                neo.setBaseModifierId(id);
            }
            return this;
        }
    }

    public static final Entry DAMAGE = entry("damage", 0, true)
            .setBaseAttributeId(AttributeModifierIDs.WEAPON_DAMAGE_ID);
    /**
     * DISPLAY-ONLY as of 3.0.0! This attribute no longer participates in pull time calculation,
     * it only exists so items can render an authentic tooltip line.
     * The actual pull time is defined by the `ranged_weapon:properties` component, see {@link RangedWeaponProperties}.
     */
    public static final Entry PULL_TIME = entry("pull_time", 0.1, 1.0, true)
            .setBaseAttributeId(AttributeModifierIDs.WEAPON_PULL_TIME_ID);
    public static final Entry HASTE = entry("haste", 100, true);
    public static final Entry VELOCITY = entry("velocity", 0, false);
}
