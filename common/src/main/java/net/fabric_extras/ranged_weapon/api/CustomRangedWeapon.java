package net.fabric_extras.ranged_weapon.api;

public interface CustomRangedWeapon {
    // Sets the baseline configuration for the weapon, multipliers are calculated compared to this
    // Already configured for known weapon types, such as BOW and CROSSBOW
    // Should only be used for custom RangedWeaponItem subclasses
    void setTypeBaseline(RangedConfig config);
    // Returns the baseline configuration for the weapon, representing the default value for a weapon type
    // Already configured for known weapon types, such as BOW and CROSSBOW
    RangedConfig getTypeBaseline();
}
