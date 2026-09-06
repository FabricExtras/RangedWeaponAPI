package net.fabric_extras.ranged_weapon.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.api.CustomBow;
import net.fabric_extras.ranged_weapon.api.CustomCrossbow;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * HeldItemRenderer checks for `ItemStack.isOf(Items.CROSSBOW)` to implement specific render angles.
 * All of these checks are wrapped to also check for our custom crossbows.
 * <p>
 * `getHandRenderType`, `getUsingItemHandRenderType` and `isChargedCrossbow` keep the `isOf` check in
 * Forge-patched 1.20.1 too. `renderFirstPersonItem` does NOT — Forge already generalised it to
 * `instanceof CrossbowItem`, which covers custom crossbows for free, hence `require = 0` there.
 */
@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    @WrapOperation(
            method = "getHandRenderType",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
    )
    private static boolean rwa_getHandRenderType_IsOf(ItemStack itemStack, Item item, Operation<Boolean> original) {
        return rwa_matches(itemStack, item) || original.call(itemStack, item);
    }

    @WrapOperation(
            method = "getUsingItemHandRenderType",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
    )
    private static boolean rwa_getUsingItemHandRenderType_IsOf(ItemStack itemStack, Item item, Operation<Boolean> original) {
        return rwa_matches(itemStack, item) || original.call(itemStack, item);
    }

    @WrapOperation(
            method = "isChargedCrossbow",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
    )
    private static boolean rwa_isChargedCrossbow_IsOf(ItemStack itemStack, Item item, Operation<Boolean> original) {
        if (item == Items.CROSSBOW && CustomCrossbow.instances.contains(itemStack.getItem())) {
            return true;
        }
        return original.call(itemStack, item);
    }

    @WrapOperation(
            method = "renderFirstPersonItem",
            require = 0, // Forge replaces the `isOf` check with `instanceof CrossbowItem`
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
    )
    private boolean rwa_renderFirstPersonItem_IsOf(ItemStack itemStack, Item item, Operation<Boolean> original) {
        if (item == Items.CROSSBOW && CustomCrossbow.instances.contains(itemStack.getItem())) {
            return true;
        }
        return original.call(itemStack, item);
    }

    private static boolean rwa_matches(ItemStack itemStack, Item item) {
        if (item == Items.CROSSBOW) {
            return CustomCrossbow.instances.contains(itemStack.getItem());
        }
        if (item == Items.BOW) {
            return CustomBow.instances.contains(itemStack.getItem());
        }
        return false;
    }
}
