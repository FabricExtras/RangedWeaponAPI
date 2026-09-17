package net.rpg_foundation.ranged_weapon.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.FirstPersonHandsAndItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.rpg_foundation.ranged_weapon.api.CustomBow;
import net.rpg_foundation.ranged_weapon.api.CustomCrossbow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FirstPersonHandsAndItems.class)
public class FirstPersonHandsAndItemsMixin {

    /**
     * FirstPersonHandsAndItems checks for `ItemStack.is(Items.BOW / Items.CROSSBOW)` to decide which hands to render.
     * All of these checks are wrapped to also check for our custom bows and crossbows.
     * (Since 26.3 this logic lives here instead of the first person renderer, and the former
     * `selectionUsingItemWhileHoldingBowLike` is inlined into `evaluateWhichHandsToRender`.)
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
}
