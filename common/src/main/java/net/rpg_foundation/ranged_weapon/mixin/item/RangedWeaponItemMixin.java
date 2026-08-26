package net.rpg_foundation.ranged_weapon.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.rpg_foundation.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponProperties;
import net.rpg_foundation.ranged_weapon.internal.ArrowExtension;
import net.rpg_foundation.ranged_weapon.internal.ScalingUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;
import java.util.function.Consumer;

@Mixin(ProjectileWeaponItem.class)
abstract class RangedWeaponItemMixin {

    /**
     * Velocity: scale the launch `speed` argument of `shootAll` by the shooter's `ranged_weapon:velocity` attribute.
     * (Since 1.21.2 the per-projectile `shoot` call happens inside a lambda, so the argument is scaled up front.)
     */
    @ModifyVariable(method = "shoot", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float applyCustomVelocity_RWA(float speed, ServerLevel world, LivingEntity shooter, InteractionHand hand, ItemStack stack) {
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
            method = "shoot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectile(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;)Lnet/minecraft/world/entity/projectile/Projectile;"))
    private Projectile applyCustomDamage_RWA(
            // Wrapped call parameters
            Projectile projectile, ServerLevel world, ItemStack projectileStack, Consumer<Projectile> beforeSpawn,
            Operation<Projectile> original,
            // Context parameters (enclosing `shootAll`)
            ServerLevel world_, LivingEntity shooter, InteractionHand hand, ItemStack stack, List<ItemStack> projectiles, float speed, float divergence, boolean critical, @Nullable LivingEntity target) {
        var properties = RangedWeaponProperties.get(stack);
        if (properties == null) {
            return original.call(projectile, world, projectileStack, beforeSpawn);
        }
        var instance = (ProjectileWeaponItem) (Object) this;
        var velocityMultiplier = rwa_velocityMultiplier(shooter, stack, properties);
        return original.call(projectile, world, projectileStack, (Consumer<Projectile>) entity -> {
            beforeSpawn.accept(entity);
            if (entity instanceof AbstractArrow projectileEntity
                    && !((ArrowExtension) entity).rwa_isModified()) {
                var rangedDamage = shooter.getAttributeValue(EntityAttributes_RangedWeapon.DAMAGE.entry);
                if (rangedDamage > 0) {
                    var multiplier = ScalingUtil.arrowDamageMultiplier(properties.damageBaseline(instance), rangedDamage, velocityMultiplier);
                    var finalDamage = ((ArrowExtension) entity).rwa_getDamage() * multiplier;
                    projectileEntity.setBaseDamage(finalDamage);
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
