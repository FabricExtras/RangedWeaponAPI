package net.rpg_foundation.ranged_weapon.mixin.ai;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.rpg_foundation.ranged_weapon.internal.MobWeaponUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Make custom ranged weapons count as an upgrade when a mob decides whether to pick up
 * a dropped weapon.
 *
 * `Mob#compareWeapons` (reached from `canReplaceCurrentItem` for the main hand, for EVERY mob that
 * picks up items — skeletons, drowned, wither skeletons, pillagers, and piglins through their
 * `super` call) ranks candidates by `stack.is(getPreferredWeaponType())` and then by
 * `minecraft:attack_damage`. A custom bow is in neither the `…_preferred_weapons` tag nor carrying
 * attack damage, so without this hook a skeleton never trades its vanilla bow for a dropped custom
 * longbow — and, worse, would trade a custom longbow away for a vanilla bow, since only the latter
 * is in the tag.
 *
 * `require = 1`: unlike the cosmetic `require = 0` render/AI hooks in this package, this is server
 * gameplay logic and must not vanish silently. The target is `private`, but identical in the vanilla
 * and NeoForge 26.1.2 trees (`Mob:595` / NF `Mob:621`); the descriptor is pinned so a signature
 * change fails the boot rather than the feature.
 */
@Mixin(Mob.class)
public abstract class MobMixin {

    @Shadow public abstract boolean canReplaceEqualItem(ItemStack newItemStack, ItemStack currentItemStack);

    @Inject(
            method = "compareWeapons(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;)Z",
            require = 1, allow = 1,
            at = @At("HEAD"), cancellable = true)
    private void preferStrongerRangedWeapon_RWA(
            ItemStack newItemStack, ItemStack currentItemStack, EquipmentSlot slot,
            CallbackInfoReturnable<Boolean> cir) {
        var mob = (Mob) (Object) this;
        var verdict = MobWeaponUtil.compareRangedWeapons(mob, newItemStack, currentItemStack, slot);
        if (verdict != null) {
            cir.setReturnValue(verdict);
            return;
        }
        if (MobWeaponUtil.rangedKindOf(newItemStack) != null
                && MobWeaponUtil.rangedKindOf(newItemStack) == MobWeaponUtil.rangedKindOf(currentItemStack)) {
            // Two ranged weapons of the same kind that RWA cannot tell apart. Vanilla's own tiebreak
            // (enchantments, then durability, then custom name) is the right answer here, but reaching
            // it through vanilla's body would first hit the preferred-weapon tag check and reject the
            // custom weapon on identity alone — so call the tiebreak directly.
            cir.setReturnValue(canReplaceEqualItem(newItemStack, currentItemStack));
        }
    }
}
