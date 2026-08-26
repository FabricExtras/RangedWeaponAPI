package net.rpg_foundation.ranged_weapon.mixin.item;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponConfig;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponProperties;
import net.rpg_foundation.ranged_weapon.internal.AttributeUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    @ModifyVariable(method = "<init>", at = @At("HEAD"), ordinal = 0)
    private static Item.Properties applyDefaultProperties(Item.Properties settings) {
        if (!AttributeUtils.hasProperties(settings)) {
            return AttributeUtils.configure(settings, RangedWeaponConfig.CROSSBOW);
        } else {
            return settings;
        }
    }

    /**
     * Apply custom pull time
     */
    @Inject(method = "getChargeDuration", at = @At("HEAD"), cancellable = true)
    private static void applyCustomPullTime_RWA(ItemStack stack, LivingEntity user, CallbackInfoReturnable<Integer> cir) {
        var properties = RangedWeaponProperties.get(stack);
        if (properties != null) {
            // Base pull time from the component, Quick Charge still composes on top
            float f = EnchantmentHelper.modifyCrossbowChargingTime(stack, user, properties.pull_time() / 20F);
            cir.setReturnValue(Mth.floor(f * 20.0F));
        }
    }
}
