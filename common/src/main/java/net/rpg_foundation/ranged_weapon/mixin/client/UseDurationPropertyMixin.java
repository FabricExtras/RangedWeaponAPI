package net.rpg_foundation.ranged_weapon.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.UseDuration;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets ranged weapons with a `ranged_weapon:properties` component use vanilla's own bow item model
 * shape (`minecraft:use_duration` with {@code "scale": 0.05}).
 * <p>
 * Vanilla reports raw ticks used, and `bow.json` hard-codes a 20-tick draw through its `scale`.
 * For stacks carrying the component the ticks are re-expressed in those "20-tick units" against
 * the stack's real (attribute-driven) pull time, so a 12-tick and a 40-tick bow both hit the
 * full-draw frame at the end of their own draw. Items without the component are untouched.
 * <p>
 * Only applies while the entity is actively using the stack; anything else (e.g. Spell Engine
 * reporting a spell-cast-driven draw) falls through to vanilla and other mixins.
 */
@Mixin(UseDuration.class)
public class UseDurationPropertyMixin {
    private static final float VANILLA_PULL_TICKS = 20F;

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void rangedWeapon_normalizePull(ItemStack stack, ClientLevel world, ItemOwner context, int seed,
                                            CallbackInfoReturnable<Float> cir) {
        var entity = context == null ? null : context.asLivingEntity();
        if (entity == null || entity.getUseItem() != stack) {
            return;
        }
        if (stack.getItem() instanceof CrossbowItem || RangedWeaponProperties.get(stack) == null) {
            return; // crossbows dispatch on `minecraft:crossbow/pull`, which is already normalized
        }
        var pullTime = RangedWeaponProperties.pullTimeTicks(stack, 20);
        if (pullTime <= 0) {
            return;
        }
        float used = UseDuration.useDuration(stack, entity);
        var remaining = ((UseDuration) (Object) this).remaining();
        float value = remaining ? Math.max(0F, pullTime - used) : used;
        cir.setReturnValue(value * VANILLA_PULL_TICKS / pullTime);
    }
}
