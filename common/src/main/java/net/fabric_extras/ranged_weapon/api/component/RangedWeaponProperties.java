package net.fabric_extras.ranged_weapon.api.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record RangedWeaponProperties(float damage, int pull_time_ticks, float arrow_velocity) {
    public static final Codec<RangedWeaponProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("damage", 6F).forGetter(x -> x.damage),
            Codec.INT.optionalFieldOf("pull_time_ticks", 0).forGetter(x -> x.pull_time_ticks),
            Codec.FLOAT.optionalFieldOf("arrow_velocity", 6F).forGetter(x -> x.arrow_velocity)
    ).apply(instance, RangedWeaponProperties::new));

    public static final float STANDARD_BOW_VELOCITY = 3F;
    public static final float STANDARD_BOW_DAMAGE = 6.0F;
    public static final float STANDARD_CROSSBOW_VELOCITY = 3.15F;
    public static final float STANDARD_CROSSBOW_DAMAGE = 9.0F;

    public static final RangedWeaponProperties BOW_BASELINE = new RangedWeaponProperties(STANDARD_BOW_DAMAGE, 20, STANDARD_BOW_VELOCITY);
    public static final RangedWeaponProperties CROSSBOW_BASELINE = new RangedWeaponProperties(STANDARD_CROSSBOW_DAMAGE, 25, STANDARD_CROSSBOW_VELOCITY);
    public static final RangedWeaponProperties EMPTY = new RangedWeaponProperties(1, 20, 1);
}