package net.rpg_foundation.ranged_weapon;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.alchemy.Potion;
import net.rpg_foundation.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponProperties;
import net.rpg_foundation.ranged_weapon.api.StatusEffects_RangedWeapon;
import java.util.List;

public class RangedWeaponMod {

    public static final String NAMESPACE = "ranged_weapon";
    public static final String ID = NAMESPACE + "_api";

    /**
     * Runs the mod initializer.
     */
    public static void init() {
        var boostEffectBonusPerLevel = 0.1;

        StatusEffects_RangedWeapon.DAMAGE.effect.addAttributeModifier(
                EntityAttributes_RangedWeapon.DAMAGE.entry,
                Identifier.fromNamespaceAndPath(NAMESPACE, "effect.damage"),
                boostEffectBonusPerLevel,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        StatusEffects_RangedWeapon.HASTE.effect.addAttributeModifier(
                EntityAttributes_RangedWeapon.HASTE.entry,
                Identifier.fromNamespaceAndPath(NAMESPACE, "effect.haste"),
                boostEffectBonusPerLevel,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    }

    public static void registerAttributes() {
        for (var entry : EntityAttributes_RangedWeapon.all) {
            entry.register();
        }
    }

    public static void registerComponents() {
        RangedWeaponProperties.register();
    }

    public static void registerStatusEffects() {
        for (var entry : StatusEffects_RangedWeapon.all) {
            entry.register();
        }
    }

    private static boolean potionRegistered = false;
    public static void registerPotions() {
        if (potionRegistered) {
            return;
        }
        potionRegistered = true;
        var entries = List.of(
                StatusEffects_RangedWeapon.DAMAGE,
                StatusEffects_RangedWeapon.HASTE
        );
        for (var entry : entries) {
            var potionId = potionId(entry.id);
            // The `baseName` is used verbatim by `PotionContentsComponent.getName(prefix)` to build
            // the item name key: `item.minecraft.<potion_item>.effect.<baseName>`.
            // Use the potion registry id path (`ranged_weapon.damage`) so the derived keys stay
            // identical to the ones produced by 1.21.1 (where `Potion.finishTranslationKey`
            // fell back to the registry key path), keeping all existing translations valid.
            var potion = new Potion(potionId.getPath(), new MobEffectInstance(entry.entry, 3600));
            Registry.register(BuiltInRegistries.POTION, potionId, potion);
        }
    }

    public static Identifier potionId(Identifier id) {
        return Identifier.fromNamespaceAndPath(id.getNamespace(), id.getNamespace() + "." + id.getPath());
    }
}
