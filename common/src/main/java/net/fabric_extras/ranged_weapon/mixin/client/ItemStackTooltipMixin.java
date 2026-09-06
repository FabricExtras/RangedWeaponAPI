package net.fabric_extras.ranged_weapon.mixin.client;

import net.fabric_extras.ranged_weapon.client.TooltipHelper;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// 1.20.1 replacement for 2.3.4's `ItemStackMixin` (which cancelled `appendAttributeModifierTooltip`,
/// a method that does not exist here) and for RWA 1.1.4's Fabric-only `ItemTooltipCallback`.
///
/// Injecting at `RETURN` also lands after Forge's `ForgeEventFactory.onItemTooltip` call, so the list
/// this sees is the final one on both loaders. The returned list is a mutable `ArrayList` in vanilla
/// and Forge alike.
@Mixin(ItemStack.class)
public class ItemStackTooltipMixin {

    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void rwa_getTooltip_RETURN(@Nullable PlayerEntity player, TooltipContext context,
                                       CallbackInfoReturnable<java.util.List<Text>> cir) {
        var lines = cir.getReturnValue();
        if (lines == null) {
            return;
        }
        try {
            TooltipHelper.updateTooltipText((ItemStack) (Object) this, lines);
        } catch (Exception ignored) {
            // Tooltip cosmetics must never take down item rendering.
        }
    }
}
