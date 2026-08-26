package net.rpg_foundation.ranged_weapon.mixin.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.CrossbowAttack;
import net.minecraft.world.item.Item;
import net.rpg_foundation.ranged_weapon.internal.MobWeaponUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Piglin crossbow combat (brain task based, unlike Pillagers' goal based combat)
 */
@Mixin(CrossbowAttack.class)
public class CrossbowAttackTaskMixin {
    /**
     * require = 0: NeoForge patches these to `isHolding(Predicate)` + `instanceof CrossbowItem`,
     * which accepts custom crossbows natively.
     */
    @WrapOperation(
            method = {"checkExtraStartConditions", "stop"},
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;isHolding(Lnet/minecraft/world/item/Item;)Z"))
    private boolean allowCustomCrossbows_RWA(Mob instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || MobWeaponUtil.isHoldingKind(instance, item);
    }
}
