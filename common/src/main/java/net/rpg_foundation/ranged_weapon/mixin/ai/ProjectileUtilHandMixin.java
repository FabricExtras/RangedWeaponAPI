package net.rpg_foundation.ranged_weapon.mixin.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.rpg_foundation.ranged_weapon.internal.MobWeaponUtil;
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
            method = "getWeaponHoldingHand",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private static boolean matchCustomRanged_RWA(ItemStack stack, Item item, Operation<Boolean> original) {
        return original.call(stack, item) || MobWeaponUtil.matchesKind(stack, item);
    }
}
