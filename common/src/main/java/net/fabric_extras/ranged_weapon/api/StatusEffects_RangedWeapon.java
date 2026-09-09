package net.fabric_extras.ranged_weapon.api;

import net.fabric_extras.ranged_weapon.internal.CustomStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class StatusEffects_RangedWeapon {
    public static final String NAMESPACE = "ranged_weapon";
    public static class Entry {
        public final Identifier id;
        public final StatusEffect effect;

        /// Filled in by {@link #register()}. On 1.20.1 the raw {@link #effect} is what the vanilla
        /// APIs take; this is kept for source compatibility with the 2.x API.
        @Nullable public RegistryEntry<StatusEffect> entry;

        public Entry(String name, int color) {
            this.id = new Identifier(NAMESPACE, name);
            this.effect = new CustomStatusEffect(StatusEffectCategory.BENEFICIAL, color);
        }

        /// Idempotent: safe to call from every platform's registration window.
        public void register() {
            if (Registries.STATUS_EFFECT.containsId(id)) {
                if (entry == null) {
                    entry = Registries.STATUS_EFFECT.getEntry(RegistryKey.of(RegistryKeys.STATUS_EFFECT, id)).orElse(null);
                }
                return;
            }
            entry = Registry.registerReference(Registries.STATUS_EFFECT, id, effect);
        }

        /// Reads {@link #entry} back out of the registry, for a loader that registered the effect itself
        /// rather than through {@link #register()} — see `RangedWeaponMod#linkStatusEffectEntries()`.
        /// Idempotent, and safe to call when the entry is already linked.
        public void link() {
            if (entry == null) {
                entry = Registries.STATUS_EFFECT
                        .getEntry(RegistryKey.of(RegistryKeys.STATUS_EFFECT, id))
                        .orElseThrow(() -> new IllegalStateException(
                                "RangedWeaponAPI status effect " + id + " is not in the registry — register it first"));
            }
        }
    }

    public static final ArrayList<Entry> all = new ArrayList<>();
    private static Entry entry(String name, int color) {
        var entry = new Entry(name, color);
        all.add(entry);
        return entry;
    }

    public static final Entry DAMAGE = entry("damage", 0xAAFFDD);
    public static final Entry HASTE = entry("haste", 0xB30000);
}
