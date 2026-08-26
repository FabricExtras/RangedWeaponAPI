package net.fabric_extras.ranged_weapon.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.api.CustomCrossbow;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {
    /**
     * Arm pose `CROSSBOW_HOLD` for custom crossbows.
     *
     * Only the crossbow pose needs a hook: vanilla gates it on the item identity
     * (`stack.isOf(Items.CROSSBOW) && CrossbowItem.isCharged(stack)`), which custom crossbows fail.
     * The bow pose needs none — vanilla reaches `BOW_AND_ARROW` through
     * `stack.getUseAction() == UseAction.BOW`, which `CustomBow` inherits from `BowItem`.
     */
    @WrapOperation(
            method = "getArmPose",
            require = 0, // For Sinytra Connector, Forge replaces the `isOf` check with `instanceof`
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
    )
    private static boolean armPose_crossbowHold_RWA(ItemStack itemStack, Item item, Operation<Boolean> original) {
        if (item == Items.CROSSBOW && CustomCrossbow.instances.contains(itemStack.getItem())) {
            return true;
        }
        return original.call(itemStack, item);
    }
}
