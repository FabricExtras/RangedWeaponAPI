package net.rpg_foundation.ranged_weapon.neoforge.client;

import net.rpg_foundation.ranged_weapon.RangedWeaponMod;
import net.rpg_foundation.ranged_weapon.client.RangedWeaponAPIClient;
import net.rpg_foundation.ranged_weapon.client.RangedWeaponItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;

@EventBusSubscriber(modid = RangedWeaponMod.ID, value = Dist.CLIENT)
public class NeoForgeClientMod {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        RangedWeaponAPIClient.init();
    }

    @SubscribeEvent
    public static void onRegisterItemModelProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(RangedWeaponItemProperties.PULL_ID, RangedWeaponItemProperties.PullProperty.CODEC);
    }
}
