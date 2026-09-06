package net.fabric_extras.ranged_weapon.client;

public class RangedWeaponAPIClient {
    /**
     * Runs the mod initializer on the client environment.
     * <p>
     * Model predicates are registered from `MinecraftClientMixin` (at `MinecraftClient.run` HEAD), so all
     * mods have had a chance to construct their items first. Tooltip formatting is done by
     * `ItemStackTooltipMixin` — 1.20.1 delta: 2.3.4 hooked `ItemStack#appendAttributeModifierTooltip`,
     * which does not exist here, and Fabric API's `ItemTooltipCallback` (what RWA 1.1.4 used) is not
     * available on the loader-neutral 1.20.1 line.
     */
    public static void init() {
    }
}
