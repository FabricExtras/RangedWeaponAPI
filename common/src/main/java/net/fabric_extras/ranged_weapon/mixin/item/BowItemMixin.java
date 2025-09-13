package net.fabric_extras.ranged_weapon.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabric_extras.ranged_weapon.api.RangedConfig;
import net.fabric_extras.ranged_weapon.internal.RangedItemSettings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BowItem.class)
public class BowItemMixin {
    public float getPullProgress_RWA(int useTicks, LivingEntity user) {
        var pullTime = user.getAttributeValue(EntityAttributes_RangedWeapon.PULL_TIME.entry);
        var pullTimeTicks = Math.round(pullTime * 20);
//        System.out.println("Bow Pull time: " + pullTimeTicks);
        float f = (float)useTicks / pullTimeTicks;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

    @ModifyVariable(method = "<init>", at = @At("HEAD"), ordinal = 0)
    private static Item.Settings applyDefaultAttributes(Item.Settings settings) {
        if (((RangedItemSettings) settings).getRangedAttributes() == null) {
            return ((RangedItemSettings) settings).rangedAttributes(RangedConfig.BOW);
        } else {
            return settings;
        }
    }

    /**
     * Apply custom pull time
     */
    @WrapOperation(
            method = "onStoppedUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BowItem;getPullProgress(I)F")
    )
    private float applyCustomPullTime(
            // Mixin parameters
            int ticks, Operation<Float> original,
            // Context parameters
            ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        return getPullProgress_RWA(ticks, user);
    }
}
