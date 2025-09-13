package net.fabric_extras.ranged_weapon.internal;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class CustomStatusEffect extends StatusEffect {
    public static String uuid = "e8222db4-6c3c-4bbe-bacb-6e8d07e96e8b";
    public CustomStatusEffect(StatusEffectCategory statusEffectCategory, int color) {
        super(statusEffectCategory, color);
    }
}
