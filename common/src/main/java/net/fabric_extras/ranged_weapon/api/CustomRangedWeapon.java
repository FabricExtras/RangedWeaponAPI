package net.fabric_extras.ranged_weapon.api;

public interface CustomRangedWeapon {
    // Sets the baseline configuration for the weapon, multipliers are calculated compared to this
    // Already configured for known weapon types, such as BOW and CROSSBOW
    // Should only be used for custom RangedWeaponItem subclasses
    void setTypeBaseline(RangedConfig config);
    // Returns the baseline configuration for the weapon, representing the default value for a weapon type
    // Already configured for known weapon types, such as BOW and CROSSBOW
    RangedConfig getTypeBaseline();

    /// The weapon's own configuration.
    ///
    /// 1.20.1 addition (no equivalent in 2.3.4, which reads everything back off the item's
    /// `AttributeModifiersComponent`): without data components the config has to live on the item
    /// instance, and `CrossbowItem#getPullTime(ItemStack)` — 1-arg on 1.20.1, so there is no shooter
    /// whose `ranged_weapon:pull_time` attribute could be read — needs it.
    RangedConfig getRangedWeaponConfig();
    void setRangedWeaponConfig(RangedConfig config);
}
