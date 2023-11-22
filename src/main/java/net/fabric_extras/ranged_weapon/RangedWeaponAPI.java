package net.fabric_extras.ranged_weapon;

import net.fabric_extras.ranged_weapon.api.CustomRangedWeaponProperties;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Items;

public class RangedWeaponAPI implements ModInitializer {
    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        ((CustomRangedWeaponProperties) Items.BOW).setCustomPullTime_RPGS(20);
        ((CustomRangedWeaponProperties) Items.CROSSBOW).setCustomPullTime_RPGS(25);
    }
}
