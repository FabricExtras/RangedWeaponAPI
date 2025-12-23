package net.testmod;

import net.fabric_extras.ranged_weapon.api.CustomBow;
import net.fabric_extras.ranged_weapon.api.CustomCrossbow;
import net.fabric_extras.ranged_weapon.api.RangedConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

public class TestMod implements ModInitializer {
    public static final String NAMESPACE = "testmod";

    @Override
    public void onInitialize() {
        Identifier customBowId = Identifier.of(NAMESPACE, "custom_longbow");
        Identifier customCrossbowId = Identifier.of(NAMESPACE, "custom_crossbow");
        RegistryKey<Item> bowKey = RegistryKey.of(RegistryKeys.ITEM, customBowId);
        RegistryKey<Item> crossbowKey = RegistryKey.of(RegistryKeys.ITEM, customCrossbowId);

        Item.Settings bowSettings = new Item.Settings()
                .maxDamage(300)
                .registryKey(bowKey);
        Item.Settings crossbowSettings = new Item.Settings()
                .maxDamage(300)
                .registryKey(crossbowKey);

        var bow = new CustomBow(
                bowSettings,
                new RangedConfig(9, 1F, 1),
                ItemTags.REPAIRS_GOLD_ARMOR
        );
        var crossbow = new CustomCrossbow(
                crossbowSettings,
                new RangedConfig(9, 1F, 1),
                ItemTags.REPAIRS_GOLD_ARMOR
        );

        Registry.register(Registries.ITEM, bowKey, bow);
        Registry.register(Registries.ITEM, crossbowKey, crossbow);
    }
}