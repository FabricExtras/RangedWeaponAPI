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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /// Populates the `entry` fields of {@link EntityAttributes_RangedWeapon} from the registry, for a loader
    /// that registered the attributes itself rather than through {@link #registerAttributes()}.
    ///
    /// Fabric gets those as the return value of `Registry.registerReference`; Forge's `RegisterEvent` helper
    /// returns void, so the Forge entrypoint calls this straight after its registration loop instead, keeping
    /// the public API identical on both loaders. Idempotent.
    public static void linkAttributeEntries() {
        for (var entry : EntityAttributes_RangedWeapon.all) {
            entry.link();
        }
    }

    public static void registerStatusEffects() {
        for (var entry : StatusEffects_RangedWeapon.all) {
            entry.register();
        }
    }

    /// {@link #linkAttributeEntries()} for {@link StatusEffects_RangedWeapon}.
    public static void linkStatusEffectEntries() {
        for (var entry : StatusEffects_RangedWeapon.all) {
            entry.link();
        }
    }

    private static boolean potionsRequested = false;
    private static boolean potionsRegistered = false;
    private static Map<Identifier, Potion> potionsToRegister = null;

    /**
     * Opt-in: registers a long-duration potion for each RangedWeaponAPI status effect.
     * <p>
     * 1.20.1 delta: vanilla registries are locked outside their registration window on Forge, so this
     * cannot always register immediately. Calling it flags the request; the actual registration then
     * happens inside the platform's potion registration window
     * (Fabric: {@code Potions.<clinit>} TAIL, Forge: {@code RegisterEvent(POTIONS)}). On Fabric, if that
     * window has already passed, the registration happens right away. Idempotent.
     * <p>
     * <b>On Forge the request must be made before {@code RegisterEvent(POTIONS)} is posted</b> — from the
     * consumer's {@code @Mod} constructor, say. There is no way to write into a vanilla registry outside its
     * own event on Forge 47.0-47.3, so a later request cannot be honoured.
     */
    public static void registerPotions() {
        potionsRequested = true;
        if (!Platform.Fabric) {
            // Forge registers these through the `RegisterEvent(POTIONS)` helper — see `ForgeMod`. A plain
            // `Registry.register` throws "Can not register to a locked registry" on 47.0-47.3, event or not.
            return;
        }
        if (Registries.POTION.getIds().isEmpty()) {
            // The vanilla `Potions` holder class has not run yet — wait for the `Potions.<clinit>` TAIL hook.
            return;
        }
        registerPotionsIfRequested();
    }

    /// Every potion RangedWeaponAPI adds, keyed by the id it registers under; empty unless
    /// {@link #registerPotions()} was called. Creation only — nothing is registered here, so a loader that
    /// registers potions itself iterates this instead of duplicating the construction. Built once.
    public static Map<Identifier, Potion> potionsToRegister() {
        if (!potionsRequested) {
            return Map.of();
        }
        if (potionsToRegister == null) {
            var potions = new LinkedHashMap<Identifier, Potion>();
            var entries = List.of(
                    StatusEffects_RangedWeapon.DAMAGE,
                    StatusEffects_RangedWeapon.HASTE
            );
            for (var entry : entries) {
                potions.put(potionId(entry.id), new Potion(new StatusEffectInstance(entry.effect, 3600)));
            }
            potionsToRegister = potions;
        }
        return potionsToRegister;
    }

    /** Called from Fabric's potion registration window. Does nothing unless opted in. */
    public static void registerPotionsIfRequested() {
        if (!potionsRequested || potionsRegistered) {
            return;
        }
        potionsRegistered = true;
        potionsToRegister().forEach((id, potion) -> {
            if (!Registries.POTION.containsId(id)) {
                Registry.register(Registries.POTION, id, potion);
            }
        });
    }

    public static Identifier potionId(Identifier id) {
        return new Identifier(id.getNamespace(), id.getNamespace() + "." + id.getPath());
    }
}
