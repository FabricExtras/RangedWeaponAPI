package net.rpg_foundation.ranged_weapon.internal;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.rpg_foundation.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponProperties;
import org.jetbrains.annotations.Nullable;

/**
 * Helpers for the AI mixins (`mixin/ai`), letting mob AI recognize custom ranged weapons
 * wherever vanilla hardcodes identity checks against `Items.BOW` / `Items.CROSSBOW`.
 */
public class MobWeaponUtil {

    /**
     * Whether the stack is a custom equivalent of the given vanilla ranged weapon:
     * same weapon kind + carries the `ranged_weapon:properties` component.
     * Intended as the `|| custom` half of wrapped `ItemStack.isOf(...)` checks.
     */
    public static boolean matchesKind(ItemStack stack, Item vanillaItem) {
        if (stack.get(RangedWeaponProperties.TYPE) == null) { // not contains(): NeoForge's Yarn jar names it `has` → NoSuchMethodError
            return false;
        }
        if (vanillaItem == Items.BOW) {
            return stack.getItem() instanceof BowItem;
        }
        if (vanillaItem == Items.CROSSBOW) {
            return stack.getItem() instanceof CrossbowItem;
        }
        return false;
    }

    /**
     * Erased-descriptor variant for the `@WrapOperation` hooks on `ItemStack.is(...)`: since 26.1 that
     * overload is the generic `TypedInstance#is(T)`, so the wrapped call site is `is(Ljava/lang/Object;)Z`
     * and the handler receives the argument as `Object`.
     */
    public static boolean matchesKind(ItemStack stack, Object vanillaItem) {
        return vanillaItem instanceof Item item && matchesKind(stack, item);
    }

    /**
     * `LivingEntity.isHolding(...)` counterpart of {@link #matchesKind}.
     */
    public static boolean isHoldingKind(LivingEntity entity, Item vanillaItem) {
        return matchesKind(entity.getMainHandItem(), vanillaItem)
                || matchesKind(entity.getOffhandItem(), vanillaItem);
    }

    /**
     * Whether the item carries the `ranged_weapon:properties` component as a default component.
     */
    public static boolean hasProperties(Item item) {
        return item.components().get(RangedWeaponProperties.TYPE) != null; // not contains(): NeoForge's Yarn jar names it `has`
    }

    /**
     * The vanilla ranged weapon archetype the stack belongs to (`Items.BOW` / `Items.CROSSBOW`),
     * or `null` when it is not a ranged weapon this mod models.
     *
     * Membership is decided by the `ranged_weapon:properties` component: `CustomBow` / `CustomCrossbow`
     * get it from their `RangedWeaponConfig` (or the deprecated `RangedConfig`, which converts to one),
     * and every other `BowItem` / `CrossbowItem` — vanilla's own included — gets a default one from
     * `item/BowItemMixin` / `item/CrossbowItemMixin`. Read off the *stack*, never off the `Item` at
     * init time: since 26.1 default components only bind to the item at resource reload.
     */
    public static @Nullable Item rangedKindOf(ItemStack stack) {
        if (RangedWeaponProperties.get(stack) == null) {
            return null;
        }
        if (stack.getItem() instanceof BowItem) {
            return Items.BOW;
        }
        if (stack.getItem() instanceof CrossbowItem) {
            return Items.CROSSBOW;
        }
        return null;
    }

    /**
     * Ranged-weapon-aware ordering for `Mob#compareWeapons`, so mobs actually upgrade to a dropped
     * custom bow/crossbow. Vanilla gates every weapon swap on `stack.is(getPreferredWeaponType())`
     * and then on `minecraft:attack_damage` — a custom bow is in neither, so a skeleton would never
     * trade its vanilla bow for a stronger one.
     *
     * Rule: **ranged damage first, then pull time** (shorter pull time wins on equal damage).
     * Returns `null` when RWA has no opinion — either the pair is not two ranged weapons it models,
     * or the two are indistinguishable by both criteria — in which case the caller must let vanilla decide.
     *
     * Only pairs of the SAME archetype are compared. Swapping a bow for a crossbow (or back) stays
     * vanilla's call, because that decides which AI goal the mob can run at all, not weapon strength:
     * a skeleton handed a "stronger" crossbow could not shoot it.
     */
    public static @Nullable Boolean compareRangedWeapons(Mob mob, ItemStack candidate, ItemStack current, EquipmentSlot slot) {
        var kind = rangedKindOf(candidate);
        if (kind == null || kind != rangedKindOf(current)) {
            return null;
        }
        var candidateDamage = rangedDamageOf(mob, candidate, slot);
        var currentDamage = rangedDamageOf(mob, current, slot);
        if (candidateDamage != currentDamage) {
            return candidateDamage > currentDamage;
        }
        var candidatePullTime = RangedWeaponProperties.get(candidate).pull_time();
        var currentPullTime = RangedWeaponProperties.get(current).pull_time();
        if (candidatePullTime != currentPullTime) {
            return candidatePullTime < currentPullTime;
        }
        return null;
    }

    /**
     * The stack's `ranged_weapon:damage` value in the given slot, mirroring the shape of
     * `Mob#getApproximateAttributeWith`. The mob's base value is the same on both sides of a
     * comparison, so it cancels out; a ranged weapon carrying no damage modifier scores the bare
     * base (0 for every mob), which is also what a vanilla bow would score if `item/BowItemMixin`
     * ever stopped attaching its defaults.
     */
    private static double rangedDamageOf(Mob mob, ItemStack stack, EquipmentSlot slot) {
        var attribute = EntityAttributes_RangedWeapon.DAMAGE.entry;
        if (attribute == null) {
            return 0;
        }
        var base = mob.getAttributes().hasAttribute(attribute) ? mob.getAttributeBaseValue(attribute) : 0.0;
        return stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY)
                .compute(attribute, base, slot);
    }
}
