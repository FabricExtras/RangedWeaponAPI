package net.fabric_extras.ranged_weapon.mixin.item;

import net.fabric_extras.ranged_weapon.api.CustomRangedWeaponProperties;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin implements CustomRangedWeaponProperties {
    /**
     * CustomRangedWeaponProperties
     */
    private int customPullTime = 0;
    @Override
    public int getCustomPullTime_RPGS() {
        return customPullTime;
    }
    @Override
    public void setCustomPullTime_RPGS(int pullTime) {
        customPullTime = pullTime;
    }

    private float customVelocity = 0;
    @Override
    public float getCustomVelocity_RPGS() {
        return customVelocity;
    }
    @Override
    public void setCustomVelocity_RPGS(float velocity) {
        customVelocity = velocity;
    }

    public float getCustomPullProgress(int useTicks) {
        float pullTime = this.customPullTime > 0 ? this.customPullTime : 20;
        float f = (float)useTicks / pullTime;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

    /**
     * Apply custom pull time
     */
    @Inject(method = "getPullTime", at = @At("HEAD"), cancellable = true)
    private static void applyCustomPullTime_SpellEngine(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        var item = stack.getItem();
        if (item instanceof CustomRangedWeaponProperties weapon) {
            var pullTime = weapon.getCustomPullTime_RPGS();
            if (pullTime > 0) {
//                var quickChargeStacks = EnchantmentHelper.getLevel(Enchantments.QUICK_CHARGE, stack);
//                pullTime -= (int) (pullTime * ArchersMod.tweaksConfig.value.quick_charge_enchantment_multiplier_per_level) * quickChargeStacks;
                cir.setReturnValue(pullTime);
                cir.cancel();
            }
        }
    }

    /**
     * Apply custom velocity
     */
    @ModifyVariable(method = "shoot", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private static float applyCustomVelocity_SpellEngine(float speed, World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative, float speed1, float divergence, float simulated) {
        var item = crossbow.getItem();
        if (item instanceof CustomRangedWeaponProperties weapon) {
            var customVelocity = weapon.getCustomVelocity_RPGS();
            if (customVelocity > 0) {
                return speed * (customVelocity / DEFAULT_SPEED);
            }
        }
        return speed;
    }
    @Shadow @Final private static float DEFAULT_SPEED;
}
