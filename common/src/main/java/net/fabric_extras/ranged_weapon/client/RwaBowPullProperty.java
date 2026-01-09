package net.fabric_extras.ranged_weapon.client;

import com.mojang.serialization.MapCodec;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.minecraft.client.render.item.property.numeric.NumericProperty;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class RwaBowPullProperty implements NumericProperty {

    public static final MapCodec<RwaBowPullProperty> CODEC = MapCodec.unit(new RwaBowPullProperty());

    @Override
    public float getValue(ItemStack stack, @Nullable ClientWorld world, @Nullable LivingEntity holder, int seed) {
        if (holder == null) return 0f;
        if (!holder.isUsingItem() || holder.getActiveItem() != stack) return 0f;

        int useTicks = stack.getMaxUseTime(holder) - holder.getItemUseTimeLeft();

        double pullTimeSeconds = holder.getAttributeValue(EntityAttributes_RangedWeapon.PULL_TIME.entry);
        int pullTimeTicks = Math.max(1, (int)Math.round(pullTimeSeconds * 20.0));

        float f = (float) useTicks / (float) pullTimeTicks;
        f = (f * f + f * 2.0F) / 3.0F;
        return Math.min(f, 1.0F);
    }

    @Override
    public MapCodec<? extends NumericProperty> getCodec() {
        return CODEC;
    }
}
