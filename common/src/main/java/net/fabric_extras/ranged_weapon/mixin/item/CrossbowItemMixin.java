package net.fabric_extras.ranged_weapon.mixin.item;

import net.fabric_extras.ranged_weapon.api.CustomRangedWeapon;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabric_extras.ranged_weapon.api.RangedConfig;
import net.fabric_extras.ranged_weapon.internal.RangedItemSettings;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
    @ModifyVariable(method = "<init>", at = @At("HEAD"), ordinal = 0)
    private static Item.Settings applyDefaultAttributes(Item.Settings settings) {
        if (((RangedItemSettings) settings).getRangedAttributes() == null) {
            return ((RangedItemSettings) settings).rangedAttributes(RangedConfig.CROSSBOW);
        } else {
            return settings;
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void postInit(Item.Settings settings, CallbackInfo ci) {
        ((CustomRangedWeapon)this).setTypeBaseline(RangedConfig.CROSSBOW);
    }

    /**
     * Apply custom pull time
     */
    @Inject(method = "getPullTime", at = @At("HEAD"), cancellable = true)
    private static void applyCustomPullTime_RWA(ItemStack stack, LivingEntity user, CallbackInfoReturnable<Integer> cir) {
        var item = stack.getItem();
        if (item instanceof CustomRangedWeapon weapon) {
            var pullTime = (float) user.getAttributeValue(EntityAttributes_RangedWeapon.PULL_TIME.entry);
            float f = EnchantmentHelper.getCrossbowChargeTime(stack, user, pullTime);
            var pullTimeTicks = MathHelper.floor(f * 20.0F);
            cir.setReturnValue(pullTimeTicks);
            cir.cancel();
        }
    }
}
