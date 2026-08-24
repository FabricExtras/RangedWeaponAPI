package net.fabric_extras.ranged_weapon.mixin.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.internal.MobWeaponUtil;
import net.minecraft.entity.ai.brain.task.CrossbowAttackTask;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Piglin crossbow combat (brain task based, unlike Pillagers' goal based combat)
 */
@Mixin(CrossbowAttackTask.class)
public class CrossbowAttackTaskMixin {
    @WrapOperation(
            method = {"shouldRun", "finishRunning"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;isHolding(Lnet/minecraft/item/Item;)Z"))
    private boolean allowCustomCrossbows_RWA(MobEntity instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || MobWeaponUtil.isHoldingKind(instance, item);
    }
}
