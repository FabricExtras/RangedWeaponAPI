package net.fabric_extras.ranged_weapon.mixin.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.internal.MobWeaponUtil;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * `getHandPossiblyHolding(entity, Items.BOW/CROSSBOW)` is how mob AI resolves which hand
 * holds the weapon (skeleton attack type + shooting, bow/crossbow goals, piglin brain task).
 * Also match custom equivalents, so off-hand custom weapons resolve correctly.
 */
@Mixin(ProjectileUtil.class)
public class ProjectileUtilHandMixin {
    @WrapOperation(
            method = "getHandPossiblyHolding",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private static boolean matchCustomRanged_RWA(ItemStack stack, Item item, Operation<Boolean> original) {
        return original.call(stack, item) || MobWeaponUtil.matchesKind(stack, item);
    }
}
