package net.rpg_foundation.ranged_weapon.compat.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;

/// EMI plugin placeholder for RangedWeaponAPI.
///
/// Anvil repair recipes used to be registered here from each `CustomBow`/`CustomCrossbow`'s repair-ingredient
/// supplier. Repairability is now expressed purely through the vanilla `minecraft:repairable` component
/// (`Item.Settings.repairable(TagKey)`), which EMI's own `VanillaPlugin` already derives anvil repair recipes
/// from — so there is nothing mod-specific left to register.
@EmiEntrypoint
public class RangedWeaponEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry registry) {
    }
}
