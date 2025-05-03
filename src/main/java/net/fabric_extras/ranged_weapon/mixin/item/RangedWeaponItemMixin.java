package net.fabric_extras.ranged_weapon.mixin.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.api.AttributeModifierIDs;
import net.fabric_extras.ranged_weapon.api.CustomRangedWeapon;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabric_extras.ranged_weapon.api.RangedConfig;
import net.fabric_extras.ranged_weapon.internal.RangedItemSettings;
import net.fabric_extras.ranged_weapon.internal.ScalingUtil;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
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
        var attributes = ((RangedItemSettings) settings).getRangedAttributes();
        if (attributes != null) {
            return settings.attributeModifiers(createAttributeModifiers(attributes));
        } else {
            return settings;
        }
    }

    private static AttributeModifiersComponent createAttributeModifiers(RangedConfig config) {
        var damage = new EntityAttributeModifier(
                AttributeModifierIDs.WEAPON_DAMAGE_ID,
                config.damage(),
                EntityAttributeModifier.Operation.ADD_VALUE);

        var pullTime = new EntityAttributeModifier(
                AttributeModifierIDs.WEAPON_PULL_TIME_ID,
                config.pull_time_bonus(),
                EntityAttributeModifier.Operation.ADD_VALUE);

        var builder = AttributeModifiersComponent.builder()
                .add(
                        EntityAttributes_RangedWeapon.DAMAGE.entry,
                        damage,
                        AttributeModifierSlot.HAND
                )
                .add(
                        EntityAttributes_RangedWeapon.PULL_TIME.entry,
                        pullTime,
                        AttributeModifierSlot.HAND
                );

        if (config.velocity_bonus() > 0) {
            var velocity = new EntityAttributeModifier(
                    AttributeModifierIDs.WEAPON_VELOCITY_ID,
                    config.velocity_bonus(),
                    EntityAttributeModifier.Operation.ADD_VALUE);
            builder
                .add(
                        EntityAttributes_RangedWeapon.VELOCITY.entry,
                        velocity,
                        AttributeModifierSlot.HAND
                );
        }


        return builder.build();
    }

    // CustomRangedWeapon

    private RangedConfig typeBaseLine = RangedConfig.BOW;

    public void setTypeBaseline(RangedConfig config) {
        this.typeBaseLine = config;
    }

    public RangedConfig getTypeBaseline() {
        return this.typeBaseLine;
    }

    @WrapOperation(
            method = "shootAll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/RangedWeaponItem;shoot(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/projectile/ProjectileEntity;IFFFLnet/minecraft/entity/LivingEntity;)V"))
    private void applyCustomVelocity_RWA(
            RangedWeaponItem instance, LivingEntity shooter, ProjectileEntity projectile, int index, float speed, float divergence, float yaw, @Nullable LivingEntity target,
            Operation<Void> original) {
        var bonusVelocity = shooter.getAttributeValue(EntityAttributes_RangedWeapon.VELOCITY.entry);
        var velocityMultiplier = ScalingUtil.arrowVelocityMultiplier(instance, bonusVelocity);
//        System.out.println("Velocity multiplier: " + velocityMultiplier);
        speed *= (float) velocityMultiplier;
        original.call(instance, shooter, projectile, index, speed, divergence, yaw, target);

        if (projectile instanceof PersistentProjectileEntity projectileEntity) {
            var rangedDamage = shooter.getAttributeValue(EntityAttributes_RangedWeapon.DAMAGE.entry);
            if (rangedDamage > 0) {
                var multiplier = ScalingUtil.arrowDamageMultiplier(getTypeBaseline().damage(), rangedDamage, velocityMultiplier);
                var finalDamage = projectileEntity.getDamage() * multiplier;
                projectileEntity.setDamage(finalDamage);
            }
        }
    }
}