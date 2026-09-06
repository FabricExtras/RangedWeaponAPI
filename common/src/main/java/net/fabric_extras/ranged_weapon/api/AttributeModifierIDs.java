package net.fabric_extras.ranged_weapon.api;

import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/// Well-known ids of the attribute modifiers RangedWeaponAPI puts on ranged weapons.
///
/// 1.20.1 note: entity attribute modifiers are keyed by `UUID` + display name (the `Identifier`-keyed
/// modifiers of 1.21.1 do not exist yet). The `Identifier` constants are kept as the canonical names —
/// they are still what a `RangedConfig.Modifier` carries — and each one gets a *deterministic* UUID
/// derived from it, so the same logical modifier always resolves to the same UUID across runs, mods
/// and game versions.
public class AttributeModifierIDs {
    public static final String NAMESPACE = "ranged_weapon";

    public static final Identifier WEAPON_DAMAGE_ID = new Identifier(NAMESPACE, "base_damage");
    public static final Identifier WEAPON_PULL_TIME_ID = new Identifier(NAMESPACE, "base_pull_time");
    public static final Identifier WEAPON_VELOCITY_ID = new Identifier(NAMESPACE, "base_velocity");
    public static final Identifier OTHER_BONUS_ID = new Identifier(NAMESPACE, "ranged_weapon");

    /// Deterministic UUID for an attribute-modifier identifier.
    /// `UUID.nameUUIDFromBytes(id.toString().getBytes(UTF_8))` — stable, collision-free in practice,
    /// and reproducible by consumers that need to look a modifier up.
    public static UUID uuid(Identifier id) {
        return UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static final UUID WEAPON_DAMAGE_UUID = uuid(WEAPON_DAMAGE_ID);
    public static final UUID WEAPON_PULL_TIME_UUID = uuid(WEAPON_PULL_TIME_ID);
    public static final UUID WEAPON_VELOCITY_UUID = uuid(WEAPON_VELOCITY_ID);
    public static final UUID OTHER_BONUS_UUID = uuid(OTHER_BONUS_ID);
}
