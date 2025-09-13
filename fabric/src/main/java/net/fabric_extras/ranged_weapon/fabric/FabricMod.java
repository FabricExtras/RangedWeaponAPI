package net.fabric_extras.ranged_weapon.fabric;

import net.fabric_extras.ranged_weapon.RangedWeaponMod;
import net.fabricmc.api.ModInitializer;

public final class FabricMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        RangedWeaponMod.init();
    }
}
