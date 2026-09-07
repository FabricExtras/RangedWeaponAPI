package net.fabric_extras.ranged_weapon.api;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CrossbowMechanics {

    /// Rebalanced **Quick Charge**.
    ///
    /// On 1.21.1 this is a data-driven enchantment override (`data/minecraft/enchantment/quick_charge.json`)
    /// that does two things at once:
    ///   * cuts the `minecraft:crossbow_charge_time` reduction from vanilla's `-0.25 s` per level down to
    ///     **`-0.05 s` per level** (one tick), and
    ///   * grants **`ranged_weapon:haste` `add_multiplied_base 0.10` per level** instead.
    ///
    /// Net effect on a 25-tick crossbow: `(25 - level) / (1 + 0.10 * level)` ticks — 21.8 / 19.2 / 16.9
    /// for Quick Charge I / II / III, against vanilla 1.20.1's 20 / 15 / 10. The enchantment is markedly
    /// weaker, and what remains of it flows through the ranged-weapon haste attribute the rest of the
    /// ecosystem already scales.
    ///
    /// 1.20.1 has no enchantment registry to override and no `EnchantmentHelper#getCrossbowChargeTime`;
    /// vanilla hardcodes `25 - 5 * level` in `CrossbowItem#getPullTime`. Both halves are therefore in
    /// code: the charge-time half in {@link PullTime#defaultProvider} below, the haste half in
    /// `mixin.attribute.LivingEntityMixin` (which is where `ranged_weapon:haste` is consumed).
    public static class QuickCharge {
        /// Ticks removed from the crossbow's pull time per level — 1.21.1's `-0.05 s`, one tick at 20 tps.
        public static final int DEFAULT_PULL_TIME_TICKS_PER_LEVEL = 1;
        /// `ranged_weapon:haste` `add_multiplied_base` amount per level.
        public static final float DEFAULT_HASTE_PER_LEVEL = 0.10F;

        public static int pullTimeTicksPerLevel = DEFAULT_PULL_TIME_TICKS_PER_LEVEL;
        public static float hastePerLevel = DEFAULT_HASTE_PER_LEVEL;

        /// The haste multiplier Quick Charge contributes, as `add_multiplied_base` resolves it.
        public static double hasteMultiplier(ItemStack crossbow) {
            int level = EnchantmentHelper.getLevel(Enchantments.QUICK_CHARGE, crossbow);
            return level <= 0 ? 1.0 : 1.0 + (double) hastePerLevel * level;
        }
    }

    public static class PullTime {
        /// Vanilla 1.20.1 hardcodes `25 - 5 * quickChargeLevel`; this applies the 1.21.1 rebalance
        /// instead — see {@link QuickCharge}. The haste half is applied separately, in
        /// `mixin.attribute.LivingEntityMixin`.
        public static final Provider defaultProvider = (originalPullTime, crossbow, user) -> {
            int quickCharge = EnchantmentHelper.getLevel(Enchantments.QUICK_CHARGE, crossbow);
            return originalPullTime - QuickCharge.pullTimeTicksPerLevel * quickCharge;
        };
        public static Provider modifier = defaultProvider;
        public interface Provider {
            /// @param user may be `null`: on 1.20.1 `CrossbowItem#getPullTime` takes only the stack,
            ///             so there is no shooter to consult at that call site.
            int getPullTime(int originalPullTime, ItemStack crossbow, @Nullable LivingEntity user);
        }
    }
}
