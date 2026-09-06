package net.fabric_extras.ranged_weapon.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.api.CustomBow;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/// `getFovMultiplier` zooms in while a bow is drawn, keyed on `isOf(Items.BOW)` and a hardcoded 20-tick
/// pull. Both checks are generalised. Verified present and identical in vanilla and Forge-patched 1.20.1
/// (Forge only swaps the final `MathHelper.lerp(...)` for `ForgeHooksClient.getFieldOfViewModifier`).
@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityMixin {
    @WrapOperation(
            method = "getFovMultiplier",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
    )
    private boolean rwa_getFovMultiplier_CustomBows(ItemStack itemStack, Item item, Operation<Boolean> original) {
        if (item == Items.BOW && CustomBow.instances.contains(itemStack.getItem())) {
            return true;
        }
        return original.call(itemStack, item);
    }

    @ModifyConstant(method = "getFovMultiplier", constant = @Constant(floatValue = 20.0F))
    private float rwa_getFovMultiplier_CustomBows_PullTime(float value) {
        var player = (AbstractClientPlayerEntity) (Object) this;
        var pullTimeTicks = (float) player.getAttributeValue(EntityAttributes_RangedWeapon.PULL_TIME.attribute) * 20F;
        return pullTimeTicks > 0 ? pullTimeTicks : value;
    }
}
