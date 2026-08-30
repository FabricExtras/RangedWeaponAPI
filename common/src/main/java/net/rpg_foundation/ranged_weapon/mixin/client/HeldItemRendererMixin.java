package net.rpg_foundation.ranged_weapon.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.rpg_foundation.ranged_weapon.api.CustomBow;
import net.rpg_foundation.ranged_weapon.api.CustomCrossbow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemInHandRenderer.class)
public class HeldItemRendererMixin {

    /**
     * HeldItemRenderer checks for `ItemStack.isOf(Items.CROSSBOW)` to implement specific render angles.
     * All of these checks are wrapped to also check for our custom crossbows.
     */

    @WrapOperation(
            method = "evaluateWhichHandsToRender",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z")
    )
    private static boolean getHandRenderType_ItemStack_IsOf_Ranged(ItemStack itemStack, Object item, Operation<Boolean> original) {
        if (item == Items.CROSSBOW) {
            if (CustomCrossbow.instances.contains(itemStack.getItem())) {
                return true;
            }
        }
        if (item == Items.BOW) {
            if (CustomBow.instances.contains(itemStack.getItem())) {
                return true;
            }
        }

        return original.call(itemStack, item);
    }

    @WrapOperation(
            method = "selectionUsingItemWhileHoldingBowLike",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z")
    )
    private static boolean getUsingItemHandRenderType_ItemStack_IsOf_Ranged(ItemStack itemStack, Object item, Operation<Boolean> original) {
        if (item == Items.CROSSBOW) {
            if (CustomCrossbow.instances.contains(itemStack.getItem())) {
                return true;
            }
        }
        if (item == Items.BOW) {
            if (CustomBow.instances.contains(itemStack.getItem())) {
                return true;
            }
        }

        return original.call(itemStack, item);
    }

    @WrapOperation(
            method = "isChargedCrossbow",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z")
    )
    private static boolean isChargedCrossbow_ItemStack_IsOf_Crossbow(ItemStack itemStack, Object item, Operation<Boolean> original) {
        if (item == Items.CROSSBOW) {
            if (CustomCrossbow.instances.contains(itemStack.getItem())) {
                return true;
            }
        }
        return original.call(itemStack, item);
    }

    @WrapOperation(
            method = "renderArmWithItem",
            require = 0, // NeoForge rewrites this branch to `itemStack.getItem() instanceof CrossbowItem`
                         // (verified on 26.1.2.94), so there is no `is` call to wrap there.
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z")
    )
    private boolean renderFirstPersonItem_ItemStack_IsOf_Crossbow(ItemStack itemStack, Object item, Operation<Boolean> original) {
        if (item == Items.CROSSBOW) {
            if (CustomCrossbow.instances.contains(itemStack.getItem())) {
                return true;
            }
        }
        return original.call(itemStack, item);
    }
}
