package net.fabric_extras.ranged_weapon.api;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;

public class CrossbowMechanics {
    public static class PullTime {
        public static final Provider defaultProvider = (originalPullTime, crossbow) -> {
            int quickCharge = EnchantmentHelper.getLevel(Enchantments.QUICK_CHARGE, crossbow);
            return originalPullTime - (int) (originalPullTime * 0.2) * quickCharge;
        };
        public static Provider modifier = defaultProvider;
        public interface Provider {
            int getPullTime(int originalPullTime, ItemStack crossbow);
        }
    }
}
