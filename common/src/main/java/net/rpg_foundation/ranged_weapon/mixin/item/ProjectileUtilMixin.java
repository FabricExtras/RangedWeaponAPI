package net.rpg_foundation.ranged_weapon.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.rpg_foundation.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponProperties;
import net.rpg_foundation.ranged_weapon.internal.ArrowExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {

    /**
     * This mixin applies to non-standard shoot cases, such as
     * - some mobs (skeletons, illusioners) shooting with bows
     */

    @WrapOperation(method = "getMobArrow",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;setBaseDamageFromMob(F)V"))
    private static void rwa$applyDamage(
            // Mixin parameters
            AbstractArrow instance, float damageModifier, Operation<Void> original,
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
            instance.setBaseDamage(((ArrowExtension)instance).rwa_getDamage() * multiplier);
            ((ArrowExtension)instance).rwa_markModified(true);
        }
    }
}
