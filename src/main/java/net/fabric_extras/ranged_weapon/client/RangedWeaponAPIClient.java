package net.fabric_extras.ranged_weapon.client;

import net.fabric_extras.ranged_weapon.api.CustomBow;
import net.fabric_extras.ranged_weapon.api.CustomCrossbow;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.mixin.registry.sync.client.MinecraftClientMixin;

public class RangedWeaponAPIClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        // Calling these from MinecraftClient run, so all mod registrations are done
//        for (var bow: CustomBow.instances) {
//            ModelPredicateHelper.registerBowModelPredicates(bow);
//        }
//        for (var crossbow: CustomCrossbow.instances) {
//            ModelPredicateHelper.registerCrossbowModelPredicates(crossbow);
//        }
    }
}
