package net.fabric_extras.ranged_weapon.api.component;

import net.fabric_extras.ranged_weapon.RangedWeaponMod;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.UnaryOperator;

public class RangedWeaponComponents {
    public static final ComponentType<RangedWeaponProperties> BASELINE = register(Identifier.of(RangedWeaponMod.ID, "baseline"),
            builder -> builder.codec(RangedWeaponProperties.CODEC)
    );

    private static <T> ComponentType<T> register(Identifier id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, id, ((ComponentType.Builder)builderOperator.apply(ComponentType.builder())).build());
    }
}
