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
 * Ranged weapons should use the <b>vanilla</b> item model shape: bows dispatch on
 * `minecraft:use_duration` with {@code "scale": 0.05} (see `assets/minecraft/items/bow.json`),
 * crossbows on `minecraft:crossbow/pull` (`crossbow.json`). For stacks carrying the
 * `ranged_weapon:properties` component, `use_duration` is re-expressed against the stack's real
 * pull time (see `mixin.client.UseDurationPropertyMixin`), so the vanilla definition animates
 * correctly whatever the pull time is — and anything else hooking the vanilla properties
 * (e.g. Spell Engine's cast-driven draw) covers these weapons too.
 * <ul>
 *   <li>{@code ranged_weapon:pull} — <b>deprecated</b>, kept so existing definitions keep loading:
 *       numeric charge progress in {@code [0, 1]} against the component's pull time
 *       (crossbows: `CrossbowItem.getPullTime`). Equivalent to the vanilla shape above.</li>
 * </ul>
 */
public class RangedWeaponItemProperties {
    public static final Identifier PULL_ID = Identifier.of(RangedWeaponProperties.ID.getNamespace(), "pull");

    public static void register() {
        NumericProperties.ID_MAPPER.put(PULL_ID, PullProperty.CODEC);
    }

    /** @deprecated use `minecraft:use_duration` (bows) / `minecraft:crossbow/pull` (crossbows), see class doc. */
    @Deprecated
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
