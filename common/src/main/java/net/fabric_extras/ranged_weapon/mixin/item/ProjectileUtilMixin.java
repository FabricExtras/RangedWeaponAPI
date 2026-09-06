package net.fabric_extras.ranged_weapon.mixin.item;

import net.fabric_extras.ranged_weapon.api.CustomRangedWeapon;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabric_extras.ranged_weapon.internal.ArrowExtension;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// This mixin covers the non-player shoot path: mobs firing bows (skeletons, illusioners, strays …),
/// which all route through `AbstractSkeletonEntity#createArrowProjectile` → `ProjectileUtil.createArrowProjectile`.
///
/// 1.20.1 delta: `createArrowProjectile(LivingEntity, ItemStack, float)` is 3-arg — it does **not**
/// receive the bow stack that 1.21.1's 4-arg version passes, and it calls `applyEnchantmentEffects`
/// rather than `applyDamageModifier`. So the weapon is resolved from the shooter's hands, and the hook
/// is a plain `RETURN` inject on the finished projectile instead of a `@WrapOperation` on the inner call.
@Mixin(ProjectileUtil.class)
public class ProjectileUtilMixin {

    @Inject(method = "createArrowProjectile", at = @At("RETURN"))
    private static void rwa_applyDamage(LivingEntity entity, ItemStack projectileStack, float damageModifier,
                                        CallbackInfoReturnable<PersistentProjectileEntity> cir) {
        var projectile = cir.getReturnValue();
        if (projectile == null || ((ArrowExtension) projectile).rwa_isModified()) {
            return;
        }
        var weaponStack = rwa_heldRangedWeapon(entity);
        if (weaponStack == null) {
            return;
        }
        var rangedWeapon = (CustomRangedWeapon) weaponStack.getItem();
        var baselineDamage = rangedWeapon.getTypeBaseline().damage();
        if (baselineDamage <= 0) {
            return;
        }
        var currentDamage = entity.getAttributeValue(EntityAttributes_RangedWeapon.DAMAGE.attribute);
        if (currentDamage <= 0) {
            return;
        }
        var multiplier = currentDamage / baselineDamage;
        projectile.setDamage(projectile.getDamage() * multiplier);
        ((ArrowExtension) projectile).rwa_markModified(true);
    }

    @Unique
    private static ItemStack rwa_heldRangedWeapon(LivingEntity entity) {
        var mainHand = entity.getMainHandStack();
        if (mainHand.getItem() instanceof CustomRangedWeapon) {
            return mainHand;
        }
        var offHand = entity.getOffHandStack();
        if (offHand.getItem() instanceof CustomRangedWeapon) {
            return offHand;
        }
        return null;
    }
}
