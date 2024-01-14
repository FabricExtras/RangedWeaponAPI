package net.fabric_extras.ranged_weapon.api;

import org.jetbrains.annotations.Nullable;

/**
 * Represents the configurable properties of ranged weapons.
 * @param pull_time - the number of ticks it takes to fully pull back the weapon
 * @param damage - the amount of damage the weapon deals
 * @param velocity - customized velocity of the projectile (if null, the default velocity is used).
 *                 Does not affect the projectile damage!
 */
public record RangedConfig(int pull_time, float damage, @Nullable Float velocity) { }
