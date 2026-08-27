package net.rpg_foundation.ranged_weapon.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.rpg_foundation.ranged_weapon.internal.MobWeaponUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Bow aiming arm pose (instead of the melee swing) for custom bows.
 * Since 1.21.2 the model reads `holdingBow` from the render state, filled by the renderer.
 * Explicit descriptors to avoid matching synthetic bridge methods.
 */
@Mixin(AbstractSkeletonRenderer.class)
public class AbstractSkeletonEntityRendererMixin {
    @WrapOperation(
            method = {
                    "extractRenderState(Lnet/minecraft/world/entity/monster/skeleton/AbstractSkeleton;Lnet/minecraft/client/renderer/entity/state/SkeletonRenderState;F)V",
                    "getArmPose(Lnet/minecraft/world/entity/monster/skeleton/AbstractSkeleton;Lnet/minecraft/world/entity/HumanoidArm;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;"
            },
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z"))
    private boolean bowPoseForCustomBows_RWA(ItemStack stack, Object item, Operation<Boolean> original) {
        return original.call(stack, item) || MobWeaponUtil.matchesKind(stack, item);
    }
}
