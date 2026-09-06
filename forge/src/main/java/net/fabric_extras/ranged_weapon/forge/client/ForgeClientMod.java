package net.fabric_extras.ranged_weapon.forge.client;

import net.fabric_extras.ranged_weapon.RangedWeaponMod;
import net.fabric_extras.ranged_weapon.client.RangedWeaponAPIClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = RangedWeaponMod.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeClientMod {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        RangedWeaponAPIClient.init();
    }
}
