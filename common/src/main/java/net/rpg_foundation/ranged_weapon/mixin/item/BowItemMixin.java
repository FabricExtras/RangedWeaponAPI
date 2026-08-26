package net.rpg_foundation.ranged_weapon.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponConfig;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponProperties;
import net.rpg_foundation.ranged_weapon.internal.AttributeUtils;
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

    @ModifyVariable(method = "<init>", at = @At("HEAD"), ordinal = 0)
    private static Item.Settings applyDefaultProperties(Item.Settings settings) {
        if (!AttributeUtils.hasProperties(settings)) {
            return AttributeUtils.configure(settings, RangedWeaponConfig.BOW);
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
        var properties = RangedWeaponProperties.get(stack);
        if (properties == null) {
            return original.call(ticks);
        }
        float f = (float)ticks / properties.pull_time();
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }
}
