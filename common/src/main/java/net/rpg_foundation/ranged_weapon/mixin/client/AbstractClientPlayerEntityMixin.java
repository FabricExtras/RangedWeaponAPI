package net.rpg_foundation.ranged_weapon.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.rpg_foundation.ranged_weapon.api.CustomBow;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractClientPlayer.class)
public class AbstractClientPlayerEntityMixin {
    @WrapOperation(
            method = "getFieldOfViewModifier",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z")
    )
    private boolean getFovMultiplier_CustomBows(ItemStack itemStack, Item item, Operation<Boolean> original) {
        if (item == Items.BOW) {
            if (CustomBow.instances.contains(itemStack.getItem())) {
                return true;
            }
        }
        return original.call(itemStack, item);
    }

    @ModifyConstant(method = "getFieldOfViewModifier", constant = @Constant(floatValue = 20.0F))
    private float getFovMultiplier_CustomBows_PullTime(float value) {
        var player = (AbstractClientPlayer)(Object)this;
        return RangedWeaponProperties.pullTimeTicks(player.getUseItem(), 20);
    }
}
