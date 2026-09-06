package net.fabric_extras.ranged_weapon.forge;

import net.fabric_extras.ranged_weapon.RangedWeaponMod;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod(RangedWeaponMod.ID)
public final class ForgeMod {

    // FMLJavaModLoadingContext.get() is flagged for removal by late 47.x builds, but the
    // constructor-injected replacement doesn't exist on early 47.x; get() works on all of [47,).
    @SuppressWarnings("removal")
    public ForgeMod() {
        RangedWeaponMod.init();

        var modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Forge locks vanilla registries outside their RegisterEvent window ("Can not register to a locked
        // registry"), so the Fabric `<clinit>`-TAIL mixins are not shipped on Forge (they live in
        // `ranged_weapon_api.fabric.mixins.json`); the same idempotent common registration functions run from
        // the per-registry event instead. Inside the window the vanilla wrapper registry delegates
        // `Registry.register` into the ForgeRegistry, so the common code needs no Forge API.
        // Explicit event class: Forge 47's plain addListener(Consumer) infers the event type from the lambda
        // via TypeTools, which is fragile; the 4-arg overload takes it directly.
        modBus.addListener(EventPriority.NORMAL, false, RegisterEvent.class, event -> {
            var key = event.getRegistryKey();
            if (key.equals(ForgeRegistries.Keys.ATTRIBUTES)) {
                RangedWeaponMod.registerAttributes();
            } else if (key.equals(ForgeRegistries.Keys.MOB_EFFECTS)) {
                RangedWeaponMod.registerStatusEffects();
            } else if (key.equals(ForgeRegistries.Keys.POTIONS)) {
                RangedWeaponMod.registerPotionsIfRequested();
            }
        });

        // Replaces Fabric's `createLivingAttributes` RETURN mixin: fired after registration, once every
        // living entity type's default attributes are built.
        modBus.addListener(EventPriority.NORMAL, false, EntityAttributeModificationEvent.class, event -> {
            for (var entityType : event.getTypes()) {
                for (var entry : EntityAttributes_RangedWeapon.all) {
                    if (!event.has(entityType, entry.attribute)) {
                        event.add(entityType, entry.attribute);
                    }
                }
            }
        });
    }
}
