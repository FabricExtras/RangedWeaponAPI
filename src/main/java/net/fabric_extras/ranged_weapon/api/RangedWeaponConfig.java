package net.fabric_extras.ranged_weapon.api;

import org.jetbrains.annotations.Nullable;

public record RangedWeaponConfig(int pull_time, float damage, @Nullable Float velocity) { }
