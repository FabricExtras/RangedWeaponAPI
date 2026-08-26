package net.testmod;

import net.rpg_foundation.ranged_weapon.api.CustomBow;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class TestMod implements ModInitializer {
    public static final String NAMESPACE = "testmod";
    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        var id = Identifier.of(NAMESPACE, "custom_longbow");
        var bow = new CustomBow(
                new Item.Settings()
                        .registryKey(RegistryKey.of(RegistryKeys.ITEM, id))
                        .maxDamage(300)
                        .repairable(ItemTags.GOLD_TOOL_MATERIALS), // repair is the vanilla component now
                new RangedWeaponConfig(9, 40, 1F, null)
        );
        Registry.register(
                Registries.ITEM,
                id,
                bow
        );
    }
}
