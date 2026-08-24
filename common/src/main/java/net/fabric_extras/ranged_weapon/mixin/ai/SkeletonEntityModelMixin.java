package net.fabric_extras.ranged_weapon.mixin.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.internal.MobWeaponUtil;
import net.minecraft.client.render.entity.model.SkeletonEntityModel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Bow aiming arm pose (instead of the melee swing) for custom bows.
 * Explicit descriptors to avoid matching synthetic bridge methods.
 */
@Mixin(SkeletonEntityModel.class)
public class SkeletonEntityModelMixin {
    @WrapOperation(
            method = {
                    "animateModel(Lnet/minecraft/entity/mob/MobEntity;FFF)V",
                    "setAngles(Lnet/minecraft/entity/mob/MobEntity;FFFFF)V"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private boolean bowPoseForCustomBows_RWA(ItemStack stack, Item item, Operation<Boolean> original) {
        return original.call(stack, item) || MobWeaponUtil.matchesKind(stack, item);
    }
}
