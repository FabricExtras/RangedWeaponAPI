package net.rpg_foundation.ranged_weapon.api;

import net.minecraft.resources.Identifier;

public class AttributeModifierIDs {
    public static final String NAMESPACE = "ranged_weapon";

    public static final Identifier WEAPON_DAMAGE_ID = Identifier.fromNamespaceAndPath(NAMESPACE, "base_damage");
    public static final Identifier WEAPON_PULL_TIME_ID = Identifier.fromNamespaceAndPath(NAMESPACE, "base_pull_time");
    public static final Identifier WEAPON_VELOCITY_ID = Identifier.fromNamespaceAndPath(NAMESPACE, "base_velocity");
    public static final Identifier OTHER_BONUS_ID = Identifier.fromNamespaceAndPath(NAMESPACE, "ranged_weapon");
}
