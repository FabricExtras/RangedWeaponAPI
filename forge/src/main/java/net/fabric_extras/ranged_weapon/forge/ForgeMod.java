package net.fabric_extras.ranged_weapon.forge;

import net.fabric_extras.ranged_weapon.RangedWeaponMod;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabric_extras.ranged_weapon.api.StatusEffects_RangedWeapon;
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

        // Registration is duplicated here rather than delegated to `common`'s registerX() methods, because a
        // plain `Registry.register` is not usable on this loader: Forge only clears the vanilla registry's own
        // lock from 47.4.0 onwards, so on 47.0-47.3 and NeoForge 1.20.1 it throws "Can not register to a locked
        // registry" even inside the correct RegisterEvent window. Our mods.toml declares
        // `loaderVersion = "[47,)"`, so those are supported configurations. The helper this event hands out is
        // the API every build of [47,) sanctions, so Forge iterates the same content `common` exposes and
        // registers it itself. `common` keeps its own vanilla-shaped registration for Fabric, where the
        // `<clinit>`-TAIL mixins in `ranged_weapon_api.fabric.mixins.json` still call it.
        //
        // `event.register` is a no-op unless its key matches the event's registry, so all three blocks are
        // declared unconditionally; Forge posts one event per registry and each block runs in exactly its own.
        //
        // Explicit event class: Forge 47's plain addListener(Consumer) infers the event type from the lambda
        // via TypeTools, which is fragile; the 4-arg overload takes it directly.
        modBus.addListener(EventPriority.NORMAL, false, RegisterEvent.class, event -> {
            event.register(ForgeRegistries.Keys.ATTRIBUTES, helper -> {
                for (var entry : EntityAttributes_RangedWeapon.all) {
                    helper.register(entry.id, entry.attribute);
                }
                // The helper returns void, so the public `RegistryEntry` fields are filled in afterwards.
                RangedWeaponMod.linkAttributeEntries();
            });

            event.register(ForgeRegistries.Keys.MOB_EFFECTS, helper -> {
                for (var entry : StatusEffects_RangedWeapon.all) {
                    helper.register(entry.id, entry.effect);
                }
                RangedWeaponMod.linkStatusEffectEntries();
            });

            // Opt-in (`RangedWeaponMod.registerPotions()`): empty unless a consumer asked for the potions
            // before this event was posted. The effects were registered one event earlier (mob_effect is 5,
            // potion is 11), so the instances these wrap are live by now.
            event.register(ForgeRegistries.Keys.POTIONS, helper ->
                    RangedWeaponMod.potionsToRegister().forEach(helper::register));
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
