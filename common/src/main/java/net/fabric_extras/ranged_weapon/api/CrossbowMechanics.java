package net.fabric_extras.ranged_weapon.api;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class CrossbowMechanics {
    public static class PullTime {
        /// 1.20.1 has no `EnchantmentHelper#getCrossbowChargeTime`; vanilla `CrossbowItem#getPullTime`
        /// hardcodes `25 - 5 * quickChargeLevel`. Expressed proportionally (20% of the weapon's own pull
        /// time per Quick Charge level) so custom pull times scale the same way vanilla's does.
        public static final Provider defaultProvider = (originalPullTime, crossbow, user) -> {
            int quickCharge = EnchantmentHelper.getLevel(Enchantments.QUICK_CHARGE, crossbow);
            return originalPullTime - (int) (originalPullTime * 0.2) * quickCharge;
        };
        public static Provider modifier = defaultProvider;
        public interface Provider {
            /// @param user may be `null`: on 1.20.1 `CrossbowItem#getPullTime` takes only the stack,
            ///             so there is no shooter to consult at that call site.
            int getPullTime(int originalPullTime, ItemStack crossbow, @Nullable LivingEntity user);
        }
    }
}
