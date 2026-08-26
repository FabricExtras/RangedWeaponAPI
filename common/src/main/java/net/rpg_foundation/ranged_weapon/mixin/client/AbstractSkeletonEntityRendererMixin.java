package net.rpg_foundation.ranged_weapon.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.rpg_foundation.ranged_weapon.internal.MobWeaponUtil;
import net.minecraft.client.render.entity.AbstractSkeletonEntityRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Bow aiming arm pose (instead of the melee swing) for custom bows.
 * Since 1.21.2 the model reads `holdingBow` from the render state, filled by the renderer.
 * Explicit descriptors to avoid matching synthetic bridge methods.
 */
@Mixin(AbstractSkeletonEntityRenderer.class)
public class AbstractSkeletonEntityRendererMixin {
    @WrapOperation(
            method = {
                    "updateRenderState(Lnet/minecraft/entity/mob/AbstractSkeletonEntity;Lnet/minecraft/client/render/entity/state/SkeletonEntityRenderState;F)V",
                    "getArmPose(Lnet/minecraft/entity/mob/AbstractSkeletonEntity;Lnet/minecraft/util/Arm;)Lnet/minecraft/client/render/entity/model/BipedEntityModel$ArmPose;"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private boolean bowPoseForCustomBows_RWA(ItemStack stack, Item item, Operation<Boolean> original) {
        return original.call(stack, item) || MobWeaponUtil.matchesKind(stack, item);
    }
}
