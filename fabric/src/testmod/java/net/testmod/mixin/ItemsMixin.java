package net.testmod.mixin;

import net.minecraft.item.Items;
import net.testmod.TestMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// Registers the probe items inside the vanilla item registration window — see {@link TestMod}.
@Mixin(Items.class)
public class ItemsMixin {
    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void static_tail_TestMod(CallbackInfo ci) {
        TestMod.registerItems();
    }
}
