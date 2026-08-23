package net.fabric_extras.ranged_weapon.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabric_extras.ranged_weapon.internal.ScalingUtil;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Weapon-physics properties of a ranged weapon, attached as the `ranged_weapon:properties` item component.
 * Presence of this component is what makes an item participate in the ranged weapon systems
 * (attribute driven damage scaling, custom pull time). Vanilla bows and crossbows receive a default
 * instance automatically.
 *
 * @param damage    OPTIONAL. The damage baseline of the weapon, acting as the divisor when converting
 *                  the shooter's `ranged_weapon:damage` attribute value into an arrow damage multiplier.
 *                  This is a weapon-physics property, NOT a balance knob: it must equal the
 *                  vanilla-equivalent full charge output of the weapon type
 *                  (arrow base damage × launch velocity; 6 for bows, 9 for crossbows).
 *                  When absent, the baseline of the known weapon type is used (recommended).
 *                  Set it only for weapon types with non-standard projectile physics
 *                  (for example third-party bow subclasses with boosted arrows).
 * @param velocity  OPTIONAL. The launch velocity baseline of the weapon, acting as the divisor
 *                  when converting the shooter's `ranged_weapon:velocity` attribute value into a
 *                  projectile speed multiplier. Weapon-physics property, coupled to `damage`:
 *                  `damage baseline = arrow base damage (2) × velocity baseline`
 *                  (3.0 for bows, 3.15 for crossbows — also the fallback when absent).
 *                  Set it only for weapon types with non-standard launch velocity.
 * @param pull_time The pull time of the weapon, in ticks. This value is definitive:
 *                  the `ranged_weapon:pull_time` entity attribute is display-only and
 *                  does not participate in the calculation.
 */
public record RangedWeaponProperties(Optional<Float> damage, Optional<Float> velocity, int pull_time) {
    public static final Identifier ID = Identifier.of(EntityAttributes_RangedWeapon.NAMESPACE, "properties");

    public static final Codec<RangedWeaponProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("damage").forGetter(RangedWeaponProperties::damage),
            Codec.FLOAT.optionalFieldOf("velocity").forGetter(RangedWeaponProperties::velocity),
            Codec.INT.fieldOf("pull_time").forGetter(RangedWeaponProperties::pull_time)
    ).apply(instance, RangedWeaponProperties::new));

    public static final PacketCodec<RegistryByteBuf, RangedWeaponProperties> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.optional(PacketCodecs.FLOAT), RangedWeaponProperties::damage,
            PacketCodecs.optional(PacketCodecs.FLOAT), RangedWeaponProperties::velocity,
            PacketCodecs.VAR_INT, RangedWeaponProperties::pull_time,
            RangedWeaponProperties::new
    );

    public static ComponentType<RangedWeaponProperties> TYPE;

    /**
     * Called from `DataComponentTypes` static initializer (via mixin), do not call.
     */
    public static void register() {
        TYPE = Registry.register(Registries.DATA_COMPONENT_TYPE, ID,
                ComponentType.<RangedWeaponProperties>builder()
                        .codec(CODEC)
                        .packetCodec(PACKET_CODEC)
                        .build());
    }

    public RangedWeaponProperties(int pull_time) {
        this(Optional.empty(), Optional.empty(), pull_time);
    }

    @Nullable
    public static RangedWeaponProperties get(ItemStack stack) {
        return stack.get(TYPE);
    }

    /**
     * The damage baseline to divide the shooter's damage attribute value by,
     * falling back to the known weapon type baselines.
     */
    public double damageBaseline(Item item) {
        return damage.map(Float::doubleValue).orElseGet(() -> ScalingUtil.baselineFor(item).damage());
    }

    /**
     * The launch velocity baseline to divide the shooter's velocity attribute value by,
     * falling back to the known weapon type baselines.
     */
    public double velocityBaseline(Item item) {
        return velocity.map(Float::doubleValue).orElseGet(() -> ScalingUtil.baselineFor(item).velocity());
    }

    /**
     * The pull time (in ticks) of the given stack, or `fallback` if it has no properties component.
     */
    public static int pullTimeTicks(ItemStack stack, int fallback) {
        var properties = get(stack);
        return properties != null ? properties.pull_time() : fallback;
    }
}
