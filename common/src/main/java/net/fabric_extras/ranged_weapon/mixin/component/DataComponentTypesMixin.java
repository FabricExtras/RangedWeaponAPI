package net.fabric_extras.ranged_weapon.mixin.component;

import net.fabric_extras.ranged_weapon.api.component.RangedWeaponComponents;
import net.minecraft.component.DataComponentTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DataComponentTypes.class)
public class DataComponentTypesMixin {

    // Static class init tail inject

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void static_init_TAIL_SpellEngine(CallbackInfo ci) {
        var asd = RangedWeaponComponents.BASELINE;// Initialize the component type
    }
}
