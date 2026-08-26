package net.rpg_foundation.ranged_weapon.mixin.attribute;

import net.minecraft.world.effect.MobEffects;
import net.rpg_foundation.ranged_weapon.RangedWeaponMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEffects.class)
public class StatusEffectsMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void static_tail_RangedWeaponAPI(CallbackInfo ci) {
        RangedWeaponMod.registerStatusEffects();
    }
}
