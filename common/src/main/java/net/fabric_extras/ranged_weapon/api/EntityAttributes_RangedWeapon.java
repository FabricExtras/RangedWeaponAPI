package net.fabric_extras.ranged_weapon.api;

import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
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
        public final EntityAttribute attribute;
        public final double baseValue;
        @Nullable public RegistryEntry<EntityAttribute> entry;

        public Entry(String name, double minValue, double baseValue, boolean tracked) {
            this.id = Identifier.of(NAMESPACE, name);
            this.translationKey = "attribute.name." + NAMESPACE + "." + name;
            this.attribute = new ClampedEntityAttribute(translationKey, baseValue, minValue, 2048).setTracked(tracked);
            this.baseValue = baseValue;
        }

        public double asMultiplier(double attributeValue) {
            return attributeValue / baseValue;
        }

        public void register() {
            entry = Registry.registerReference(Registries.ATTRIBUTE, id, attribute);
        }
    }

    public static final Entry DAMAGE = entry("damage", 0, true);
    public static final Entry PULL_TIME = entry("pull_time", 0.1, 1.0, true);
    public static final Entry HASTE = entry("haste", 100, true);
    public static final Entry VELOCITY = entry("velocity", 0, false);
}
