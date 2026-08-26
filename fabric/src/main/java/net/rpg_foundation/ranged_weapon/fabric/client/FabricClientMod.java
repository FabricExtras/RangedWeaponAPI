package net.rpg_foundation.ranged_weapon.fabric.client;

import net.rpg_foundation.ranged_weapon.client.RangedWeaponAPIClient;
import net.rpg_foundation.ranged_weapon.client.RangedWeaponItemProperties;
import net.fabricmc.api.ClientModInitializer;

public final class FabricClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        RangedWeaponAPIClient.init();
        RangedWeaponItemProperties.register();
    }
}
