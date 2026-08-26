package net.rpg_foundation.ranged_weapon.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.rpg_foundation.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponProperties;
import net.rpg_foundation.ranged_weapon.internal.ArrowExtension;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {

    /**
     * This mixin applies to non-standard shoot cases, such as
     * - some mobs (skeletons, illusioners) shooting with bows
     */

    @WrapOperation(method = "createArrowProjectile",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;applyDamageModifier(F)V"))
    private static void rwa$applyDamage(
            // Mixin parameters
            PersistentProjectileEntity instance, float damageModifier, Operation<Void> original,
            // Context parameters
            LivingEntity entity, ItemStack projectile, float damageModifier2, @Nullable ItemStack bow
    ) {
        original.call(instance, damageModifier);
        if (bow == null) {
            return;
        }
        var properties = RangedWeaponProperties.get(bow);
        if ( !((ArrowExtension)instance).rwa_isModified()
                && properties != null) {
            var currentDamage = entity.getAttributeValue(EntityAttributes_RangedWeapon.DAMAGE.entry);
            var multiplier = currentDamage / properties.damageBaseline(bow.getItem());
            instance.setDamage(((ArrowExtension)instance).rwa_getDamage() * multiplier);
            ((ArrowExtension)instance).rwa_markModified(true);
        }
    }
}
