package net.fabric_extras.ranged_weapon.mixin.client;

import net.fabric_extras.ranged_weapon.api.CustomBow;
import net.fabric_extras.ranged_weapon.api.CustomCrossbow;
import net.fabric_extras.ranged_weapon.client.ModelPredicateHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// Registered here rather than from a client entrypoint so every mod has already constructed its items.
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "run", at = @At("HEAD"))
    private void rwa_run_HEAD(CallbackInfo ci) {
        for (var bow : CustomBow.instances) {
            ModelPredicateHelper.registerBowModelPredicates(bow);
        }
        // The vanilla bow's `pull` predicate hardcodes 20 ticks; re-register it so haste and
        // `ranged_weapon:pull_time` drive the draw animation for it too.
        ModelPredicateHelper.registerBowModelPredicates(Items.BOW);
        for (var crossbow : CustomCrossbow.instances) {
            ModelPredicateHelper.registerCrossbowModelPredicates(crossbow);
        }
    }
}
