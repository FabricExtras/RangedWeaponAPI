package net.fabric_extras.ranged_weapon.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.api.CustomBow;
import net.fabric_extras.ranged_weapon.api.CustomCrossbow;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    /**
     * Arm pose "CROSSBOW_HOLD" for any crossbow.
     * In 1.21.2 this is now derived from PlayerEntityRenderState.HandState in updateHandState().
     * Wraps the isOf(Items.CROSSBOW) check used to compute hasChargedCrossbow.
     */
    @WrapOperation(
            method = "updateHandState",
            require = 0, // Sinytra Connector / NeoForge may rewrite the callsite
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
    )
    private boolean armPose_crossbowHold_RWA(ItemStack itemStack, Item item, Operation<Boolean> original) {
        if (item == Items.CROSSBOW) {
            if (CustomCrossbow.instances.contains(itemStack.getItem())) {
                return true;
            }
        }
        return original.call(itemStack, item);
    }

    /**
     * Arm pose "BOW_AND_ARROW" / "CROSSBOW_CHARGE" while using the item.
     * In 1.21.2 updateHandState stores itemUseAction from itemStack.getUseAction().
     * If custom items don't return expected UseAction, force it here.
     */
    @WrapOperation(
            method = "updateHandState",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getUseAction()Lnet/minecraft/item/consume/UseAction;")
    )
    private UseAction armPose_useAction_RWA(ItemStack itemStack, Operation<UseAction> original) {
        if (CustomCrossbow.instances.contains(itemStack.getItem())) {
            return UseAction.CROSSBOW;
        }
        if (CustomBow.instances.contains(itemStack.getItem())) {
            return UseAction.BOW;
        }
        return original.call(itemStack);
    }

    /**
     * Fallback for environments where Connector/NeoForge rewrites the callsites and one or both WrapOperations do not apply.
     * This runs after vanilla computed the HandState and adjusts only what we need.
     */
    @Inject(method = "updateHandState", at = @At("TAIL"), require = 0)
    private void armPose_fallback_RWA(AbstractClientPlayerEntity player, PlayerEntityRenderState.HandState handState, Hand hand, CallbackInfo info) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isEmpty()) {
            return;
        }

        if (CustomCrossbow.instances.contains(stack.getItem())) {
            // Only set if vanilla didn't already do it
            if (!handState.hasChargedCrossbow) {
                handState.hasChargedCrossbow = CrossbowItem.isCharged(stack);
            }
            if (handState.itemUseAction != UseAction.CROSSBOW) {
                handState.itemUseAction = UseAction.CROSSBOW;
            }
        }
        if (CustomBow.instances.contains(stack.getItem())) {
            if (handState.itemUseAction != UseAction.BOW) {
                handState.itemUseAction = UseAction.BOW;
            }
        }
    }
}