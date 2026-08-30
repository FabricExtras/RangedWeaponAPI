package net.rpg_foundation.ranged_weapon.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.rpg_foundation.ranged_weapon.api.CustomCrossbow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AvatarRenderer.class)
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
            // require = 0 is deliberate: this is a cosmetic third-person pose, and a descriptor drift
            // should degrade the pose rather than fail the boot. NB as of NeoForge 26.1.2.94 this hook
            // does match on both loaders (NeoForge keeps `is(Items.CROSSBOW)` here verbatim; it only adds
            // an `IClientItemExtensions#getArmPose` escape hatch above it), so a miss means real drift.
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Ljava/lang/Object;)Z")
    )
    private static boolean armPose_crossbowHold_RWA(ItemStack itemStack, Object item, Operation<Boolean> original) {
        if (item == Items.CROSSBOW && CustomCrossbow.instances.contains(itemStack.getItem())) {
            return true;
        }
        return original.call(itemStack, item);
    }
}
