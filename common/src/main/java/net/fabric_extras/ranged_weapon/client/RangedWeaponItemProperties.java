package net.fabric_extras.ranged_weapon.client;

import com.mojang.serialization.MapCodec;
import net.fabric_extras.ranged_weapon.api.RangedWeaponProperties;
import net.minecraft.client.render.item.property.numeric.NumericProperties;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.render.item.property.numeric.UseDurationProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HeldItemContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Item model properties (`assets/<ns>/items/<id>.json`) for ranged weapons.
 * Replaces the pre-1.21.4 model predicates (`pull` / `pulling`), which were registered in code.
 *
 * <ul>
 *   <li>{@code ranged_weapon:pull} — numeric, the charge progress of the weapon in {@code [0, 1]},
 *       resolved against the pull time of the `ranged_weapon:properties` component
 *       (for crossbows: `CrossbowItem.getPullTime`, so Quick Charge composes on top).
 *       Use it instead of `minecraft:use_duration` + a hardcoded `scale`.</li>
 * </ul>
 * `pulling` is `minecraft:using_item`, and crossbow `charged`/`firework` are `minecraft:charge_type`.
 */
public class RangedWeaponItemProperties {
    public static final Identifier PULL_ID = Identifier.of(RangedWeaponProperties.ID.getNamespace(), "pull");

    public static void register() {
        NumericProperties.ID_MAPPER.put(PULL_ID, PullProperty.CODEC);
    }

    public static class PullProperty implements NumericProperty {
        public static final MapCodec<PullProperty> CODEC = MapCodec.unit(new PullProperty());

        @Override
        public float getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable HeldItemContext context, int seed) {
            var entity = context == null ? null : context.getEntity();
            if (entity == null || entity.getActiveItem() != stack) {
                return 0.0F;
            }
            if (stack.getItem() instanceof CrossbowItem) {
                if (CrossbowItem.isCharged(stack)) {
                    return 0.0F;
                }
                return (float) UseDurationProperty.getTicksUsedSoFar(stack, entity) / CrossbowItem.getPullTime(stack, entity);
            }
            var pullTime = RangedWeaponProperties.pullTimeTicks(stack, 20);
            if (pullTime <= 0) {
                return 0.0F;
            }
            return (float) UseDurationProperty.getTicksUsedSoFar(stack, entity) / pullTime;
        }

        @Override
        public MapCodec<PullProperty> getCodec() {
            return CODEC;
        }
    }
}
