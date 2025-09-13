package net.fabric_extras.neoforge;

import net.neoforged.fml.common.Mod;

import net.fabric_extras.ExampleMod;

@Mod(ExampleMod.MOD_ID)
public final class ExampleModNeoForge {
    public ExampleModNeoForge() {
        // Run our common setup.
        ExampleMod.init();
    }
}
