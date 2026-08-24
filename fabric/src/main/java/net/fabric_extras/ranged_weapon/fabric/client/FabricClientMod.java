package net.fabric_extras.ranged_weapon.fabric.client;

import net.fabric_extras.ranged_weapon.client.RangedWeaponAPIClient;
import net.fabric_extras.ranged_weapon.client.RangedWeaponItemProperties;
import net.fabricmc.api.ClientModInitializer;

public final class FabricClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RangedWeaponAPIClient.init();
        RangedWeaponItemProperties.register();
    }
}
