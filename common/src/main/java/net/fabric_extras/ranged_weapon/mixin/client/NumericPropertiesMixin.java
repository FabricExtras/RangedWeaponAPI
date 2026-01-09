package net.fabric_extras.ranged_weapon.mixin.client;

import net.fabric_extras.ranged_weapon.client.RwaBowPullProperty;
import net.minecraft.client.render.item.property.numeric.NumericProperties;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NumericProperties.class)
public class NumericPropertiesMixin {

    private static final Identifier RWA_BOW_PULL_ID = Identifier.of("ranged_weapon", "bow_pull");

    @Inject(method = "bootstrap", at = @At("HEAD"))
    private static void rwa_registerBowPull(CallbackInfo ci) {
        NumericPropertiesAccessor.rwa_idMapper().put(RWA_BOW_PULL_ID, RwaBowPullProperty.CODEC);
    }
}
