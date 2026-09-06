package net.fabric_extras.ranged_weapon.fabric.mixin;

import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// Fabric: attach RangedWeaponAPI's attributes to every living entity's default attribute container.
/// (Forge uses `EntityAttributeModificationEvent` instead — see `ForgeMod`.)
@Mixin(LivingEntity.class)
public abstract class LivingEntityAttributesMixin {
    @Inject(
            method = "createLivingAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;",
            require = 1, allow = 1, at = @At("RETURN")
    )
    private static void addAttributes_RangedWeaponAPI(final CallbackInfoReturnable<DefaultAttributeContainer.Builder> info) {
        var builder = info.getReturnValue();
        for (var entry : EntityAttributes_RangedWeapon.all) {
            builder.add(entry.attribute);
        }
    }
}
