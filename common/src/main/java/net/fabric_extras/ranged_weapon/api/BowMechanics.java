package net.fabric_extras.ranged_weapon.api;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class BowMechanics {

    /// Percentage-based **Power**.
    ///
    /// On 1.21.1 this is a data-driven enchantment override (`data/minecraft/enchantment/power.json`):
    /// the vanilla `minecraft:damage` effect is dropped and Power instead puts an `add_multiplied_base`
    /// modifier of `0.08 * level` on `ranged_weapon:damage`. Because that attribute's own base is 0 and
    /// the held weapon contributes an `ADD_VALUE` modifier, `add_multiplied_base` resolves to
    /// `weaponDamage * 0.08 * level` — i.e. Power is exactly `+8% arrow damage per level`, so it scales
    /// with the weapon tier instead of being a flat `+0.5 * level + 0.5`.
    ///
    /// 1.20.1 has no enchantment registry to override, so the same rebalance is done in code. Both
    /// vanilla sites that apply Power route through this class:
    /// `BowItem#onStoppedUsing` (the player path) and `PersistentProjectileEntity#applyEnchantmentEffects`
    /// (the mob path). Crossbows never apply Power on 1.20.1, and the 1.21.1 override keeps Power
    /// `supported_items: #minecraft:enchantable/bow`, so there is nothing to do for them.
    ///
    /// Applying it multiplicatively at the vanilla site is equivalent to 1.21.1's attribute route: RWA's
    /// own `ranged_weapon:damage` multiplier is applied later (at `World#spawnEntity`) and multiplication
    /// commutes, so the arrow ends up at `base * (1 + 0.08 * level) * rwaMultiplier` either way.
    public static class Power {
        /// Matches `power.json`'s `minecraft:linear` `base 0.08 / per_level_above_first 0.08`.
        public static final float DEFAULT_MULTIPLIER_PER_LEVEL = 0.08F;

        /// Consumers may retune this; `0` restores vanilla-flat behaviour only in the sense that the
        /// percentage bonus vanishes — the flat bonus stays suppressed.
        public static float multiplierPerLevel = DEFAULT_MULTIPLIER_PER_LEVEL;

        public static double damageMultiplier(int level) {
            return level <= 0 ? 1.0 : 1.0 + (double) multiplierPerLevel * level;
        }

        /// Player path: the level comes off the bow being fired.
        public static double damageMultiplier(ItemStack bow) {
            return damageMultiplier(EnchantmentHelper.getLevel(Enchantments.POWER, bow));
        }

        /// Mob path: vanilla reads Power off the shooter's equipment, not off a single stack.
        public static double damageMultiplierOf(LivingEntity shooter) {
            return damageMultiplier(EnchantmentHelper.getEquipmentLevel(Enchantments.POWER, shooter));
        }
    }
}
