package net.fabric_extras.ranged_weapon.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.api.CustomRangedWeapon;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabric_extras.ranged_weapon.internal.ArrowExtension;
import net.fabric_extras.ranged_weapon.internal.ScalingUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/// 1.20.1 bow shooting is inline in `BowItem.onStoppedUsing` — there is no `RangedWeaponItem.shootAll`
/// to wrap like on 1.21.1, so pull time hooks `BowItem.getPullProgress(I)F` and velocity/damage hook the
/// `World.spawnEntity` call. Both call sites survive Forge's patch (which only adds `ArrowLooseEvent`
/// and `customArrow`).
@Mixin(BowItem.class)
public class BowItemMixin {

    @Unique
    private float rwa_getPullProgress(int useTicks, LivingEntity user) {
        var pullTime = user.getAttributeValue(EntityAttributes_RangedWeapon.PULL_TIME.attribute);
        var pullTimeTicks = Math.max(1, Math.round(pullTime * 20));
        float f = (float) useTicks / pullTimeTicks;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

    /**
     * Apply custom pull time
     */
    @WrapOperation(
            method = "onStoppedUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/BowItem;getPullProgress(I)F")
    )
    private float rwa_applyCustomPullTime(
            // Mixin parameters
            int ticks, Operation<Float> original,
            // Context parameters
            ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        return rwa_getPullProgress(ticks, user);
    }

    /**
     * Apply custom velocity and damage.
     * <p>
     * The multipliers come from the shooter's `ranged_weapon:velocity` / `ranged_weapon:damage`
     * attributes, which the held weapon contributes to through its hand-slot attribute modifiers.
     */
    @WrapOperation(
            method = "onStoppedUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z")
    )
    private boolean rwa_applyCustomVelocityAndDamage(
            // Mixin parameters
            World instance, Entity entity, Operation<Boolean> original,
            // Context parameters
            ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (entity instanceof PersistentProjectileEntity projectile
                && stack.getItem() instanceof CustomRangedWeapon weapon) {
            var baseline = weapon.getTypeBaseline();

            var bonusVelocity = user.getAttributeValue(EntityAttributes_RangedWeapon.VELOCITY.attribute);
            var velocityMultiplier = ScalingUtil.arrowVelocityMultiplier(stack.getItem(), bonusVelocity);
            if (velocityMultiplier != 1) {
                projectile.setVelocity(projectile.getVelocity().multiply(velocityMultiplier));
            }

            if (!((ArrowExtension) projectile).rwa_isModified()) {
                var rangedDamage = user.getAttributeValue(EntityAttributes_RangedWeapon.DAMAGE.attribute);
                if (rangedDamage > 0) {
                    var multiplier = ScalingUtil.arrowDamageMultiplier(baseline.damage(), rangedDamage, velocityMultiplier);
                    projectile.setDamage(projectile.getDamage() * multiplier);
                    ((ArrowExtension) projectile).rwa_markModified(true);
                }
            }
        }
        return original.call(instance, entity);
    }
}
