package net.fabric_extras.ranged_weapon.api;

import net.fabric_extras.ranged_weapon.Platform;
import net.fabric_extras.ranged_weapon.internal.NeoAttribute;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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
        /// The raw attribute. **1.20.1**: this is what `LivingEntity#getAttributeValue` and
        /// `DefaultAttributeContainer.Builder#add` take — prefer it over {@link #entry}.
        public final EntityAttribute attribute;
        public final double baseValue;
        /// Filled in by {@link #register()}. Kept for source compatibility with the 2.x API; on 1.20.1
        /// nothing in the vanilla attribute API accepts a `RegistryEntry`, so this is informational only.
        @Nullable public RegistryEntry<EntityAttribute> entry;

        public Entry(String name, double minValue, double baseValue, boolean tracked) {
            this.id = new Identifier(NAMESPACE, name);
            this.translationKey = "attribute.name." + NAMESPACE + "." + name;
            this.attribute = Platform.util().makeAttribute(translationKey, baseValue, minValue, 2048).setTracked(tracked);
            this.baseValue = baseValue;
        }

        public double asMultiplier(double attributeValue) {
            return attributeValue / baseValue;
        }

        /// Idempotent: safe to call from every platform's registration window.
        public void register() {
            if (Registries.ATTRIBUTE.containsId(id)) {
                if (entry == null) {
                    entry = Registries.ATTRIBUTE.getEntry(RegistryKey.of(RegistryKeys.ATTRIBUTE, id)).orElse(null);
                }
                return;
            }
            entry = Registry.registerReference(Registries.ATTRIBUTE, id, attribute);
        }

        /// Reads {@link #entry} back out of the registry, for a loader that registered the attribute
        /// itself rather than through {@link #register()} — see `RangedWeaponMod#linkAttributeEntries()`.
        /// Idempotent, and safe to call when the entry is already linked.
        public void link() {
            if (entry == null) {
                entry = Registries.ATTRIBUTE
                        .getEntry(RegistryKey.of(RegistryKeys.ATTRIBUTE, id))
                        .orElseThrow(() -> new IllegalStateException(
                                "RangedWeaponAPI attribute " + id + " is not in the registry — register it first"));
            }
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
    public static final Entry PULL_TIME = entry("pull_time", 0.1, 1.0, true)
            .setBaseAttributeId(AttributeModifierIDs.WEAPON_PULL_TIME_ID);
    public static final Entry HASTE = entry("haste", 100, true);
    public static final Entry VELOCITY = entry("velocity", 0, false);
}
