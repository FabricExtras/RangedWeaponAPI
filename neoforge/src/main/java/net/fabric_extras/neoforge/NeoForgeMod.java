package net.fabric_extras.neoforge;

import net.fabric_extras.ranged_weapon.RangedWeaponMod;
import net.neoforged.fml.common.Mod;

@Mod(RangedWeaponMod.ID)
public final class NeoForgeMod {
    public NeoForgeMod() {
        RangedWeaponMod.init();
    }
}
