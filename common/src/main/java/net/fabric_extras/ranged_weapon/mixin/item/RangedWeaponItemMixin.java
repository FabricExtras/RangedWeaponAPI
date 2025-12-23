package net.fabric_extras.ranged_weapon.mixin.item;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.api.CustomRangedWeapon;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabric_extras.ranged_weapon.api.RangedConfig;
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

    private RangedConfig typeBaseLine = RangedConfig.BOW;

    public void setTypeBaseline(RangedConfig config) {
        this.typeBaseLine = config;
    }

    public RangedConfig getTypeBaseline() {
        return this.typeBaseLine;
    }

    @WrapMethod(method = "shootAll")
    private void rwa_wrapShootAll(
            ServerWorld world, LivingEntity shooter, Hand hand, ItemStack stack, List<ItemStack> projectiles,
            float speed, float divergence, boolean critical, @Nullable LivingEntity target,
            Operation<Void> original
    ) {
        var bonusVelocity = shooter.getAttributeValue(EntityAttributes_RangedWeapon.VELOCITY.entry);
        var velocityMultiplier = ScalingUtil.arrowVelocityMultiplier((RangedWeaponItem)(Object)this, bonusVelocity);

        speed *= (float) velocityMultiplier;
        original.call(world, shooter, hand, stack, projectiles, speed, divergence, critical, target);
    }

    @WrapOperation(
            method = "shootAll",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/projectile/ProjectileEntity;spawn(Lnet/minecraft/entity/projectile/ProjectileEntity;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Ljava/util/function/Consumer;)Lnet/minecraft/entity/projectile/ProjectileEntity;"
            )
    )
    private ProjectileEntity rwa_wrapSpawn_damageOnly(
            ProjectileEntity projectileEntity,
            ServerWorld world,
            ItemStack projectileStack,
            Consumer<ProjectileEntity> consumer,
            Operation<ProjectileEntity> original,
            ServerWorld world2, LivingEntity shooter, Hand hand, ItemStack weaponStack, List<ItemStack> projectiles,
            float speed, float divergence, boolean critical, @Nullable LivingEntity target
    ) {
        Consumer<ProjectileEntity> wrapped = (projectile) -> {
            consumer.accept(projectile); // runs vanilla shoot()

            if (projectile instanceof PersistentProjectileEntity p && !((ArrowExtension) projectile).rwa_isModified()) {
                var bonusVelocity = shooter.getAttributeValue(EntityAttributes_RangedWeapon.VELOCITY.entry);
                var velocityMultiplier = ScalingUtil.arrowVelocityMultiplier((RangedWeaponItem)(Object)this, bonusVelocity);

                var rangedDamage = shooter.getAttributeValue(EntityAttributes_RangedWeapon.DAMAGE.entry);
                if (rangedDamage > 0) {
                    var multiplier = ScalingUtil.arrowDamageMultiplier(getTypeBaseline().damage(), rangedDamage, velocityMultiplier);
                    p.setDamage(p.getDamage() * multiplier);
                    ((ArrowExtension) projectile).rwa_markModified(true);
                }
            }
        };

        return original.call(projectileEntity, world, projectileStack, wrapped);
    }
}