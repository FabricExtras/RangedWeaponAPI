package net.fabric_extras.ranged_weapon.mixin.client;

import net.fabric_extras.ranged_weapon.api.CustomCrossbow;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(
            method = "getArmPose(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/Hand;)Lnet/minecraft/client/render/entity/model/BipedEntityModel$ArmPose;",
            at = @At("RETURN"),
            cancellable = true
    )
    private static void rwa_crossbowHoldForCustom(
            PlayerEntity player, ItemStack stack, Hand hand,
            CallbackInfoReturnable<BipedEntityModel.ArmPose> cir
    ) {
        if (stack.isEmpty()) {
            return;
        }
        if (!player.handSwinging && stack.getItem() instanceof CustomCrossbow && CrossbowItem.isCharged(stack)) {
            cir.setReturnValue(BipedEntityModel.ArmPose.CROSSBOW_HOLD);
        }
    }
}