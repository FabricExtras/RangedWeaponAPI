package net.fabric_extras.ranged_weapon;

import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabric_extras.ranged_weapon.api.StatusEffects_RangedWeapon;
import net.fabric_extras.ranged_weapon.internal.CustomStatusEffect;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.List;

public class RangedWeaponMod {

    public static final String NAMESPACE = "ranged_weapon";
    public static final String ID = NAMESPACE + "_api";

    /**
     * Runs the mod initializer.
     */
    public static void init() {
        var boostEffectBonusPerLevel = 0.1;

        // 1.20.1: status effect modifiers are keyed by a UUID *string* and take the raw attribute
        // (1.21.1 takes a `RegistryEntry` + an `Identifier`).
        StatusEffects_RangedWeapon.DAMAGE.effect.addAttributeModifier(
                EntityAttributes_RangedWeapon.DAMAGE.attribute,
                CustomStatusEffect.uuid,
                boostEffectBonusPerLevel,
                EntityAttributeModifier.Operation.MULTIPLY_BASE);
        StatusEffects_RangedWeapon.HASTE.effect.addAttributeModifier(
                EntityAttributes_RangedWeapon.HASTE.attribute,
                CustomStatusEffect.uuid,
                boostEffectBonusPerLevel,
                EntityAttributeModifier.Operation.MULTIPLY_BASE);
    }

    public static void registerAttributes() {
        for (var entry : EntityAttributes_RangedWeapon.all) {
            entry.register();
        }
    }

    public static void registerStatusEffects() {
        for (var entry : StatusEffects_RangedWeapon.all) {
            entry.register();
        }
    }

    private static boolean potionsRequested = false;
    private static boolean potionsRegistered = false;

    /**
     * Opt-in: registers a long-duration potion for each RangedWeaponAPI status effect.
     * <p>
     * 1.20.1 delta: vanilla registries are locked outside their registration window on Forge, so this
     * cannot always register immediately. Calling it flags the request; the actual registration then
     * happens inside the platform's potion registration window
     * (Fabric: {@code Potions.<clinit>} TAIL, Forge: {@code RegisterEvent(POTIONS)}). If the window has
     * already passed (Fabric), the registration happens right away. Idempotent.
     */
    public static void registerPotions() {
        potionsRequested = true;
        if (Registries.POTION.getIds().isEmpty()) {
            // The vanilla `Potions` holder class has not run yet — wait for the platform window.
            return;
        }
        registerPotionsIfRequested();
    }

    /** Called from each platform's potion registration window. Does nothing unless opted in. */
    public static void registerPotionsIfRequested() {
        if (!potionsRequested || potionsRegistered) {
            return;
        }
        potionsRegistered = true;
        var entries = List.of(
                StatusEffects_RangedWeapon.DAMAGE,
                StatusEffects_RangedWeapon.HASTE
        );
        for (var entry : entries) {
            var id = potionId(entry.id);
            if (Registries.POTION.containsId(id)) {
                continue;
            }
            var potion = new Potion(new StatusEffectInstance(entry.effect, 3600));
            Registry.register(Registries.POTION, id, potion);
        }
    }

    public static Identifier potionId(Identifier id) {
        return new Identifier(id.getNamespace(), id.getNamespace() + "." + id.getPath());
    }
}
