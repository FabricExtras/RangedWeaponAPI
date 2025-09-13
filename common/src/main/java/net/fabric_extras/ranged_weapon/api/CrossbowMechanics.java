package net.fabric_extras.ranged_weapon.api;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class CrossbowMechanics {
    public static class PullTime {
        public static final Provider defaultProvider = (originalPullTime, crossbow, user) -> {
            float f = EnchantmentHelper.getCrossbowChargeTime(crossbow, user, 1.25F);
            return MathHelper.floor(f * 20.0F);
        };
        public static Provider modifier = defaultProvider;
        public interface Provider {
            int getPullTime(int originalPullTime, ItemStack crossbow, LivingEntity user);
        }
    }
}
