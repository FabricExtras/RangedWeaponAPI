package net.fabric_extras.ranged_weapon.mixin.item;

import com.google.common.collect.Multimap;
import net.fabric_extras.ranged_weapon.api.CustomRangedWeapon;
import net.fabric_extras.ranged_weapon.api.RangedConfig;
import net.fabric_extras.ranged_weapon.internal.AttributeUtils;
import net.fabric_extras.ranged_weapon.internal.RangedItemSettings;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.RangedWeaponItem;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/// 1.20.1 shape of 2.3.4's `RangedWeaponItemMixin`.
///
/// 2.3.4 turned the `RangedConfig` into an `AttributeModifiersComponent` inside a `@ModifyVariable`
/// on the `Item.Settings` constructor argument (once per concrete class: `BowItem`, `CrossbowItem`,
/// `RangedWeaponItem`). There are no data components on 1.20.1, so instead a single `<init>`-TAIL
/// injection on the shared `RangedWeaponItem` constructor builds a Guava `Multimap` and the item
/// serves it from `getAttributeModifiers(EquipmentSlot)`. At TAIL `this` is already the concrete
/// instance, so the weapon-type baseline can be picked with a plain `instanceof` — one injection
/// point instead of three constructor-local rewrites.
///
/// Forge honours this: `IForgeItem#getAttributeModifiers(EquipmentSlot, ItemStack)` delegates to the
/// vanilla `getAttributeModifiers(EquipmentSlot)` by default.
@Mixin(RangedWeaponItem.class)
abstract class RangedWeaponItemMixin extends Item implements CustomRangedWeapon {

    RangedWeaponItemMixin(Settings settings) {
        super(settings);
    }

    @Unique @Nullable private Multimap<EntityAttribute, EntityAttributeModifier> rwa_modifiers = null;
    @Unique private RangedConfig rwa_typeBaseline = RangedConfig.BOW;
    @Unique private RangedConfig rwa_config = RangedConfig.EMPTY;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void rwa_init(Item.Settings settings, CallbackInfo ci) {
        Object self = this;
        // Weapon-type baseline: what a vanilla weapon of this kind would do, the reference every
        // multiplier is computed against.
        if (self instanceof CrossbowItem) {
            this.rwa_typeBaseline = RangedConfig.CROSSBOW;
        } else if (self instanceof BowItem) {
            this.rwa_typeBaseline = RangedConfig.BOW;
        }

        var config = ((RangedItemSettings) settings).getRangedAttributes();
        if (config == null) {
            // Vanilla bows/crossbows (and any subclass built without a RangedConfig) get the baseline,
            // so `ranged_weapon:damage` / `pull_time` are meaningful for them too.
            config = this.rwa_typeBaseline;
        }
        setRangedWeaponConfig(config);
    }

    @Override
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        var isHandSlot = slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND;
        return (isHandSlot && this.rwa_modifiers != null)
                ? this.rwa_modifiers
                : super.getAttributeModifiers(slot);
    }

    // MARK: CustomRangedWeapon

    @Override
    public void setTypeBaseline(RangedConfig config) {
        this.rwa_typeBaseline = config;
    }

    @Override
    public RangedConfig getTypeBaseline() {
        return this.rwa_typeBaseline;
    }

    @Override
    public RangedConfig getRangedWeaponConfig() {
        return this.rwa_config;
    }

    @Override
    public void setRangedWeaponConfig(RangedConfig config) {
        this.rwa_config = config != null ? config : RangedConfig.EMPTY;
        this.rwa_modifiers = AttributeUtils.fromRangedConfig(this.rwa_config);
    }
}
