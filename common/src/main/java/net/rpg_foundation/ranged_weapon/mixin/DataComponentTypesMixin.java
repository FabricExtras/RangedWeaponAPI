package net.rpg_foundation.ranged_weapon.mixin;

import net.rpg_foundation.ranged_weapon.RangedWeaponMod;
import net.minecraft.component.DataComponentTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataComponentTypes.class)
public class DataComponentTypesMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void static_tail_RangedWeaponAPI(CallbackInfo ci) {
        RangedWeaponMod.registerComponents();
    }
}
