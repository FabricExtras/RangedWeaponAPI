package net.fabric_extras.ranged_weapon.mixin.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.internal.MobWeaponUtil;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/// The **firing** step of mob crossbow combat, and the one site with no counterpart on the 1.21.1 line.
///
/// `CrossbowUser.shoot(LivingEntity, float)` is the default method both `PillagerEntity.attack` and
/// `PiglinEntity.attack` delegate to. On 1.20.1 vanilla its guard is `entity.isHolding(Items.CROSSBOW)`;
/// on 1.21.1 vanilla it is already `itemStack.getItem() instanceof CrossbowItem`, which is why RWA 3.x
/// ships no mixin here. Without this hook a mob holding a custom crossbow charges, reaches
/// `READY_TO_ATTACK`, calls `attack(...)` — and nothing is spawned; the goal/task then resets the
/// charged flag and the mob loops silently forever.
///
/// require = 0: Forge 47 patches the guard to `isHolding(Predicate)` + `instanceof CrossbowItem`,
/// so this is a Fabric-only fix.
///
/// The `ProjectileUtil.getHandPossiblyHolding(entity, Items.CROSSBOW)` on the line above needs no hook
/// of its own — `ai.ProjectileUtilHandMixin` wraps the `ItemStack.isOf` inside that method, so every
/// caller (this one included) resolves custom weapons correctly.
///
/// Explicit descriptor: `CrossbowUser` declares three overloads named `shoot`.
@Mixin(CrossbowUser.class)
public interface CrossbowUserMixin {
    @WrapOperation(
            method = "shoot(Lnet/minecraft/entity/LivingEntity;F)V",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isHolding(Lnet/minecraft/item/Item;)Z"))
    private boolean allowCustomCrossbows_RWA(LivingEntity instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || MobWeaponUtil.isHoldingKind(instance, item);
    }
}
