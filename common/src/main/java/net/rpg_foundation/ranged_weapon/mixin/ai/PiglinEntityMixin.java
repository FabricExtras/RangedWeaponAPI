package net.rpg_foundation.ranged_weapon.mixin.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.rpg_foundation.ranged_weapon.internal.MobWeaponUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Piglin.class)
public class PiglinEntityMixin {

    /**
     * `CROSSBOW_HOLD` pose for custom crossbows (common code: fixes both logic and rendering).
     * require = 0: NeoForge patches this to `isHolding(Predicate)` + `instanceof CrossbowItem`,
     * which accepts custom crossbows natively.
     */
    @WrapOperation(
            method = "getArmPose",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/Piglin;isHolding(Lnet/minecraft/world/item/Item;)Z"))
    private boolean allowCustomCrossbows_RWA(Piglin instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || MobWeaponUtil.isHoldingKind(instance, item);
    }

    @Inject(method = "canUseNonMeleeWeapon", at = @At("HEAD"), cancellable = true)
    private void canUseCustomCrossbows_RWA(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof CrossbowItem && MobWeaponUtil.hasProperties(stack.getItem())) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Piglins value custom crossbows for pickup/equip decisions, same as vanilla ones.
     * Since 1.21.2 vanilla resolves this via the `minecraft:piglin_preferred_weapons` item tag,
     * so custom crossbows are treated as members of that tag here (wraps both `isIn(tag)` checks).
     */
    @WrapOperation(
            method = "canReplaceCurrentItem(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;)Z",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean valueCustomCrossbows_RWA(ItemStack stack, TagKey<Item> tag, Operation<Boolean> original) {
        return original.call(stack, tag)
                || (tag == ItemTags.PIGLIN_PREFERRED_WEAPONS && MobWeaponUtil.matchesKind(stack, Items.CROSSBOW));
    }
}
