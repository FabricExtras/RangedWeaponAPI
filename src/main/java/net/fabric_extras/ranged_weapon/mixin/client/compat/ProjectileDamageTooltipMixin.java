package net.fabric_extras.ranged_weapon.mixin.client.compat;

import net.fabric_extras.ranged_weapon.client.TooltipUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.projectile_damage.client.TooltipHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(TooltipHelper.class)
public class ProjectileDamageTooltipMixin {
    @Inject(method = "updateTooltipText", at = @At("TAIL"))
    private static void addPullTime(ItemStack itemStack, List<Text> lines, CallbackInfo ci) {
        TooltipUtil.addPullTime(itemStack, lines);
    }
}
