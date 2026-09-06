package net.fabric_extras.ranged_weapon.fabric.mixin;

import net.fabric_extras.ranged_weapon.RangedWeaponMod;
import net.minecraft.potion.Potions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// Potion registration is opt-in (`RangedWeaponMod.registerPotions()`); this only fulfils a request
/// that was already made, so the window exists on both loaders.
@Mixin(value = Potions.class, priority = 10000)
public class PotionsMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void static_tail_RangedWeaponAPI(CallbackInfo ci) {
        RangedWeaponMod.registerPotionsIfRequested();
    }
}
