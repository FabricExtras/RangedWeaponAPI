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

import java.util.List;

@Mixin(RangedWeaponItem.class)
abstract class RangedWeaponItemMixin {

    @WrapOperation(
            method = "shootAll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/RangedWeaponItem;shoot(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/projectile/ProjectileEntity;IFFFLnet/minecraft/entity/LivingEntity;)V"))
    private void applyCustomVelocityAndDamage_RWA(
            // Wrapped call parameters
            RangedWeaponItem instance, LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float yaw, @Nullable LivingEntity target,
            Operation<Void> original,
            // Context parameters (enclosing `shootAll`)
            ServerWorld world, LivingEntity shooter_, Hand hand, ItemStack stack, List<ItemStack> projectiles, float speed_, float divergence_, boolean critical, @Nullable LivingEntity target_) {
        var properties = RangedWeaponProperties.get(stack);
        if (properties == null) {
            original.call(instance, shooter, projectile, index, speed, divergence, yaw, target);
            return;
        }

        var bonusVelocity = shooter.getAttributeValue(EntityAttributes_RangedWeapon.VELOCITY.entry);
        var velocityBaseline = properties.velocityBaseline(instance);
        var velocityMultiplier = (velocityBaseline + bonusVelocity) / velocityBaseline;
        speed *= (float) velocityMultiplier;
        original.call(instance, shooter, projectile, index, speed, divergence, yaw, target);

        if (projectile instanceof PersistentProjectileEntity projectileEntity
            && !((ArrowExtension)projectile).rwa_isModified() ) {
            var rangedDamage = shooter.getAttributeValue(EntityAttributes_RangedWeapon.DAMAGE.entry);
            if (rangedDamage > 0) {
                var multiplier = ScalingUtil.arrowDamageMultiplier(properties.damageBaseline(instance), rangedDamage, velocityMultiplier);
                var finalDamage = projectileEntity.getDamage() * multiplier;
                projectileEntity.setDamage(finalDamage);
                ((ArrowExtension)projectile).rwa_markModified(true);
            }
        }
    }
}
