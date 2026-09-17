package net.rpg_foundation.ranged_weapon.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.rpg_foundation.ranged_weapon.api.CustomCrossbow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class HeldItemRendererMixin {

    /**
     * The first person renderer checks for `ItemStack.is(Items.CROSSBOW)` to implement specific render angles.
     * The check is wrapped to also accept our custom crossbows.
     * (The hand selection checks are in `FirstPersonHandsAndItemsMixin`.)
     */

    @WrapOperation(
            method = "submitArmWithItem",
            require = 0, // NeoForge rewrites this branch to `itemStack.getItem() instanceof CrossbowItem`
                         // (verified on 26.3.0.3-beta), so there is no `is` call to wrap there.
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
