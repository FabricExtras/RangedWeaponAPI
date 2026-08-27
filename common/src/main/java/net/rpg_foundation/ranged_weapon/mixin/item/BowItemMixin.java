package net.rpg_foundation.ranged_weapon.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponConfig;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponProperties;
import net.rpg_foundation.ranged_weapon.internal.AttributeUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BowItem.class)
public class BowItemMixin {

    @ModifyVariable(method = "<init>", at = @At("HEAD"), ordinal = 0)
    private static Item.Properties applyDefaultProperties(Item.Properties settings) {
        // Delayed (reload-time) step: only applies if no explicit `ranged_weapon:properties` was configured
        return AttributeUtils.configureDefaults(settings, RangedWeaponConfig.BOW);
    }

    /**
     * Apply custom pull time
     */
    @WrapOperation(
            method = "releaseUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BowItem;getPowerForTime(I)F")
    )
    private float applyCustomPullTime(
            // Mixin parameters
            int ticks, Operation<Float> original,
            // Context parameters
            ItemStack stack, Level world, LivingEntity user, int remainingUseTicks) {
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
