package net.rpg_foundation.ranged_weapon.mixin.client;

import net.rpg_foundation.ranged_weapon.api.RangedWeaponProperties;
import net.minecraft.client.render.item.property.numeric.UseDurationProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;
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
@Mixin(UseDurationProperty.class)
public class UseDurationPropertyMixin {
    private static final float VANILLA_PULL_TICKS = 20F;

    @Inject(method = "getValue", at = @At("HEAD"), cancellable = true)
    private void rangedWeapon_normalizePull(ItemStack stack, ClientWorld world, HeldItemContext context, int seed,
                                            CallbackInfoReturnable<Float> cir) {
        var entity = context == null ? null : context.getEntity();
        if (entity == null || entity.getActiveItem() != stack) {
            return;
        }
        if (stack.getItem() instanceof CrossbowItem || RangedWeaponProperties.get(stack) == null) {
            return; // crossbows dispatch on `minecraft:crossbow/pull`, which is already normalized
        }
        var pullTime = RangedWeaponProperties.pullTimeTicks(stack, 20);
        if (pullTime <= 0) {
            return;
        }
        float used = UseDurationProperty.getTicksUsedSoFar(stack, entity);
        var remaining = ((UseDurationProperty) (Object) this).remaining();
        float value = remaining ? Math.max(0F, pullTime - used) : used;
        cir.setReturnValue(value * VANILLA_PULL_TICKS / pullTime);
    }
}
