package net.fabric_extras.ranged_weapon.api;

import net.minecraft.util.Identifier;

public class AttributeModifierIDs {
    public static final String NAMESPACE = "ranged_weapon";

    public static final Identifier WEAPON_DAMAGE_ID = Identifier.of(NAMESPACE, "base_damage");
    public static final Identifier WEAPON_PULL_TIME_ID = Identifier.of(NAMESPACE, "base_pull_time");
    public static final Identifier WEAPON_VELOCITY_ID = Identifier.of(NAMESPACE, "base_velocity");
}
