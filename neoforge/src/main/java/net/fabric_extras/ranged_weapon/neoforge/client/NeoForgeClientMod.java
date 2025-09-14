package net.fabric_extras.ranged_weapon.neoforge.client;

import net.fabric_extras.ranged_weapon.RangedWeaponMod;
import net.fabric_extras.ranged_weapon.client.RangedWeaponAPIClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = RangedWeaponMod.ID, value = Dist.CLIENT)
public class NeoForgeClientMod {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        RangedWeaponAPIClient.init();
    }
}