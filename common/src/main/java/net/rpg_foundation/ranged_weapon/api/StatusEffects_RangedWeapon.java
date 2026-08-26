package net.rpg_foundation.ranged_weapon.api;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.rpg_foundation.ranged_weapon.internal.CustomStatusEffect;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class StatusEffects_RangedWeapon {
    public static final String NAMESPACE = "ranged_weapon";
    public static class Entry {
        public final Identifier id;
        public final MobEffect effect;

        @Nullable public Holder<MobEffect> entry;

        public Entry(String name, int color) {
            this.id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
            this.effect = new CustomStatusEffect(MobEffectCategory.BENEFICIAL, color);
        }

        public void register() {
            entry = Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, id, effect);
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
