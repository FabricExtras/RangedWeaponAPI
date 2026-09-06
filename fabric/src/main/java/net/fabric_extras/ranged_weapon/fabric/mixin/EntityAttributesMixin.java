package net.fabric_extras.ranged_weapon.fabric.mixin;

import net.fabric_extras.ranged_weapon.RangedWeaponMod;
import net.minecraft.entity.attribute.EntityAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// Fabric: register the `ranged_weapon:*` attributes as the vanilla attribute holder class finishes.
/// (Forge uses `RegisterEvent(ATTRIBUTES)` instead — see `ForgeMod`.)
@Mixin(value = EntityAttributes.class, priority = 10000) // Low priority to be applied last
public class EntityAttributesMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void static_tail_RangedWeaponAPI(CallbackInfo ci) {
        RangedWeaponMod.registerAttributes();
    }
}
