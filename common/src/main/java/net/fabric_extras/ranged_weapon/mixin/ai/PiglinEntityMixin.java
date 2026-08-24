package net.fabric_extras.ranged_weapon.mixin.ai;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.internal.MobWeaponUtil;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinEntity.class)
public class PiglinEntityMixin {

    /**
     * `CROSSBOW_HOLD` pose for custom crossbows (common code: fixes both logic and rendering).
     * require = 0: NeoForge patches this to `isHolding(Predicate)` + `instanceof CrossbowItem`,
     * which accepts custom crossbows natively.
     */
    @WrapOperation(
            method = "getActivity",
            require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/PiglinEntity;isHolding(Lnet/minecraft/item/Item;)Z"))
    private boolean allowCustomCrossbows_RWA(PiglinEntity instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || MobWeaponUtil.isHoldingKind(instance, item);
    }

    @Inject(method = "canUseRangedWeapon", at = @At("HEAD"), cancellable = true)
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
            method = "prefersNewEquipment",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"))
    private boolean valueCustomCrossbows_RWA(ItemStack stack, TagKey<Item> tag, Operation<Boolean> original) {
        return original.call(stack, tag)
                || (tag == ItemTags.PIGLIN_PREFERRED_WEAPONS && MobWeaponUtil.matchesKind(stack, Items.CROSSBOW));
    }
}
