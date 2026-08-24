package net.fabric_extras.ranged_weapon.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabric_extras.ranged_weapon.api.RangedWeaponProperties;
import net.fabric_extras.ranged_weapon.internal.ArrowExtension;
import net.fabric_extras.ranged_weapon.internal.ScalingUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;
import java.util.function.Consumer;

@Mixin(RangedWeaponItem.class)
abstract class RangedWeaponItemMixin {

    /**
     * Velocity: scale the launch `speed` argument of `shootAll` by the shooter's `ranged_weapon:velocity` attribute.
     * (Since 1.21.2 the per-projectile `shoot` call happens inside a lambda, so the argument is scaled up front.)
     */
    @ModifyVariable(method = "shootAll", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float applyCustomVelocity_RWA(float speed, ServerWorld world, LivingEntity shooter, Hand hand, ItemStack stack) {
        var properties = RangedWeaponProperties.get(stack);
        if (properties == null) {
            return speed;
        }
        return speed * (float) rwa_velocityMultiplier(shooter, stack, properties);
    }

    /**
     * Damage: after the projectile got its velocity (in the `beforeSpawn` consumer), scale its damage
     * by the shooter's `ranged_weapon:damage` attribute.
     */
    @WrapOperation(
            method = "shootAll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileEntity;spawn(Lnet/minecraft/entity/projectile/ProjectileEntity;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Ljava/util/function/Consumer;)Lnet/minecraft/entity/projectile/ProjectileEntity;"))
    private ProjectileEntity applyCustomDamage_RWA(
            // Wrapped call parameters
            ProjectileEntity projectile, ServerWorld world, ItemStack projectileStack, Consumer<ProjectileEntity> beforeSpawn,
            Operation<ProjectileEntity> original,
            // Context parameters (enclosing `shootAll`)
            ServerWorld world_, LivingEntity shooter, Hand hand, ItemStack stack, List<ItemStack> projectiles, float speed, float divergence, boolean critical, @Nullable LivingEntity target) {
        var properties = RangedWeaponProperties.get(stack);
        if (properties == null) {
            return original.call(projectile, world, projectileStack, beforeSpawn);
        }
        var instance = (RangedWeaponItem) (Object) this;
        var velocityMultiplier = rwa_velocityMultiplier(shooter, stack, properties);
        return original.call(projectile, world, projectileStack, (Consumer<ProjectileEntity>) entity -> {
            beforeSpawn.accept(entity);
            if (entity instanceof PersistentProjectileEntity projectileEntity
                    && !((ArrowExtension) entity).rwa_isModified()) {
                var rangedDamage = shooter.getAttributeValue(EntityAttributes_RangedWeapon.DAMAGE.entry);
                if (rangedDamage > 0) {
                    var multiplier = ScalingUtil.arrowDamageMultiplier(properties.damageBaseline(instance), rangedDamage, velocityMultiplier);
                    var finalDamage = ((ArrowExtension) entity).rwa_getDamage() * multiplier;
                    projectileEntity.setDamage(finalDamage);
                    ((ArrowExtension) entity).rwa_markModified(true);
                }
            }
        });
    }

    private double rwa_velocityMultiplier(LivingEntity shooter, ItemStack stack, RangedWeaponProperties properties) {
        var bonusVelocity = shooter.getAttributeValue(EntityAttributes_RangedWeapon.VELOCITY.entry);
        var velocityBaseline = properties.velocityBaseline(stack.getItem());
        return (velocityBaseline + bonusVelocity) / velocityBaseline;
    }
}
