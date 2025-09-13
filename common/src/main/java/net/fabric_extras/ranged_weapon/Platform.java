package net.fabric_extras.ranged_weapon;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.entity.attribute.ClampedEntityAttribute;

import java.nio.file.Path;

public class Platform {
    public static final boolean Fabric;
    public static final boolean Forge;
    public static final boolean NeoForge;

    static
    {
        Fabric = getPlatformType() == Type.FABRIC;
        Forge  = getPlatformType() == Type.FORGE;
        NeoForge = getPlatformType() == Type.NEOFORGE;
    }

    public enum Type { FABRIC, FORGE, NEOFORGE }

    @ExpectPlatform
    protected static Type getPlatformType() {
        throw new AssertionError();
    }

    public interface Util {
        boolean isModLoaded(String modid);
        ClampedEntityAttribute makeAttribute(String translationKey, double fallback, double min, double max);
    }

    @ExpectPlatform
    public static Util util() {
        throw new AssertionError();
    }
}
