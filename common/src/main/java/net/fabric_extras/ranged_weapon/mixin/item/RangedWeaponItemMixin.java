package net.fabric_extras.ranged_weapon.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.api.CustomRangedWeapon;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabric_extras.ranged_weapon.api.RangedConfig;
import net.fabric_extras.ranged_weapon.api.component.RangedWeaponComponents;
import net.fabric_extras.ranged_weapon.api.component.RangedWeaponProperties;
import net.fabric_extras.ranged_weapon.internal.ArrowExtension;
import net.fabric_extras.ranged_weapon.internal.RangedItemSettings;
import net.fabric_extras.ranged_weapon.internal.ScalingUtil;
import net.fabric_extras.ranged_weapon.internal.AttributeUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.RangedWeaponItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(RangedWeaponItem.class)
abstract class RangedWeaponItemMixin extends Item implements CustomRangedWeapon {
    RangedWeaponItemMixin(Settings settings) {
        super(settings);
    }

    @ModifyVariable(method = "<init>", at = @At("HEAD"), ordinal = 0)
    private static Item.Settings applyDefaultAttributes(Item.Settings settings) {
        var rangedSettings = ((RangedItemSettings) settings);
        var config = rangedSettings.getRangedAttributes();
        if (config != null) {
            AttributeModifiersComponent existingAttributes = null;
            var componentBuilder = rangedSettings.rwa_getComponentBuilder();
            if (componentBuilder != null) {
                var existingComponents = ((ComponentMapBuilderAccessor) componentBuilder).rwa_components();
                var existing = existingComponents.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
                if (existing instanceof AttributeModifiersComponent attributeModifiers) {
                    existingAttributes = attributeModifiers;
                }
            }
            var rangedAttributes = AttributeUtils.fromRangedConfig(config);
            var applicableAttributes = AttributeUtils.mergeComponents(rangedAttributes, existingAttributes);
            return settings.attributeModifiers(applicableAttributes);
        } else {
            return settings;
        }
    }

    // CustomRangedWeapon

    public void setTypeBaseline(RangedConfig config) {
        // No longer does anything, as baseline is stored in a component (set via Item.Settings)
    }

    public RangedWeaponProperties getTypeBaseline() {
        return this.getComponents().getOrDefault(RangedWeaponComponents.BASELINE, RangedWeaponProperties.EMPTY);
    }

    @WrapOperation(
            method = "shootAll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/RangedWeaponItem;shoot(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/projectile/ProjectileEntity;IFFFLnet/minecraft/entity/LivingEntity;)V"))
    private void applyCustomVelocity_RWA(
            RangedWeaponItem instance, LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float yaw, @Nullable LivingEntity target,
            Operation<Void> original) {

        var typeBaseline = getTypeBaseline();
        var bonusVelocity = shooter.getAttributeValue(EntityAttributes_RangedWeapon.VELOCITY.entry);
        var velocityMultiplier = ScalingUtil.arrowVelocityMultiplier(typeBaseline, bonusVelocity);
//        System.out.println("Velocity multiplier: " + velocityMultiplier);
        speed *= (float) velocityMultiplier;
        original.call(instance, shooter, projectile, index, speed, divergence, yaw, target);

        if (projectile instanceof PersistentProjectileEntity projectileEntity
            && !((ArrowExtension)projectile).rwa_isModified() ) {
            var rangedDamage = shooter.getAttributeValue(EntityAttributes_RangedWeapon.DAMAGE.entry);
            if (rangedDamage > 0) {
                var multiplier = ScalingUtil.arrowDamageMultiplier(typeBaseline, rangedDamage, velocityMultiplier);
                var finalDamage = projectileEntity.getDamage() * multiplier;
                projectileEntity.setDamage(finalDamage);
                ((ArrowExtension)projectile).rwa_markModified(true);
            }
        }
    }
}