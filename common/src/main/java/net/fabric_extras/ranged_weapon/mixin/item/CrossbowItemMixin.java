package net.fabric_extras.ranged_weapon.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.api.CrossbowMechanics;
import net.fabric_extras.ranged_weapon.api.CustomRangedWeapon;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabric_extras.ranged_weapon.internal.ArrowExtension;
import net.fabric_extras.ranged_weapon.internal.ScalingUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// 1.20.1 crossbow shooting is `CrossbowItem.shootAll` → private static `shoot` (once per projectile,
/// so multishot is covered). Identical in vanilla and the Forge-patched class apart from Forge's
/// `ArrowLooseEvent` guard around `shootAll`'s body.
@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {

    /**
     * Apply custom pull time.
     * <p>
     * 1.20.1 delta: `getPullTime` takes only the stack (1.21.1 takes the shooter too), so the pull time
     * comes off the weapon's own `RangedConfig` rather than the shooter's `ranged_weapon:pull_time`
     * attribute. Haste is unaffected by this — it is applied in `LivingEntityMixin#getItemUseTimeLeft`,
     * which speeds up the *progress*, not the total.
     */
    @Inject(method = "getPullTime", at = @At("HEAD"), cancellable = true)
    private static void rwa_applyCustomPullTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() instanceof CustomRangedWeapon weapon) {
            var pullTime = weapon.getRangedWeaponConfig().pullTimeTicks();
            pullTime = CrossbowMechanics.PullTime.modifier.getPullTime(pullTime, stack, null);
            cir.setReturnValue(Math.max(0, pullTime));
        }
    }

    /**
     * Apply custom velocity — `speed` is the second `float` argument of the private static `shoot`.
     */
    @ModifyVariable(method = "shoot", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    private static float rwa_applyCustomVelocity(
            float speed,
            World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile,
            float soundPitch, boolean creative, float speed1, float divergence, float simulated) {
        if (crossbow.getItem() instanceof CustomRangedWeapon) {
            var bonusVelocity = shooter.getAttributeValue(EntityAttributes_RangedWeapon.VELOCITY.attribute);
            var velocityMultiplier = ScalingUtil.arrowVelocityMultiplier(crossbow.getItem(), bonusVelocity);
            return (float) (speed * velocityMultiplier);
        }
        return speed;
    }

    /**
     * Apply custom damage
     */
    @WrapOperation(
            method = "shoot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z")
    )
    private static boolean rwa_applyCustomDamage(
            // Mixin parameters
            World instance, Entity entity, Operation<Boolean> original,
            // Context parameters
            World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectileStack,
            float soundPitch, boolean creative, float speed, float divergence, float simulated) {
        if (entity instanceof PersistentProjectileEntity projectile
                && crossbow.getItem() instanceof CustomRangedWeapon weapon
                && !((ArrowExtension) projectile).rwa_isModified()) {
            var rangedDamage = shooter.getAttributeValue(EntityAttributes_RangedWeapon.DAMAGE.attribute);
            if (rangedDamage > 0) {
                var bonusVelocity = shooter.getAttributeValue(EntityAttributes_RangedWeapon.VELOCITY.attribute);
                var velocityMultiplier = ScalingUtil.arrowVelocityMultiplier(crossbow.getItem(), bonusVelocity);
                var multiplier = ScalingUtil.arrowDamageMultiplier(weapon.getTypeBaseline().damage(), rangedDamage, velocityMultiplier);
                projectile.setDamage(projectile.getDamage() * multiplier);
                ((ArrowExtension) projectile).rwa_markModified(true);
            }
        }
        return original.call(instance, entity);
    }
}
