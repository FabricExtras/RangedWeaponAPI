package net.fabric_extras.ranged_weapon.fabric;

import net.fabric_extras.ranged_weapon.Platform;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.attribute.ClampedEntityAttribute;

import java.nio.file.Path;

public class PlatformImpl {
    public static Platform.Type getPlatformType() {
        return Platform.Type.FABRIC;
    }

    public static class FabricUtil implements Platform.Util {
        @Override
        public boolean isModLoaded(String modid) {
            return FabricLoader.getInstance().isModLoaded(modid);
        }

        @Override
        public ClampedEntityAttribute makeAttribute(String translationKey, double fallback, double min, double max) {
            return new ClampedEntityAttribute(translationKey, fallback, min, max);
        }
    }
    private static final Platform.Util UTIL = new FabricUtil();
    public static Platform.Util util() {
        return UTIL;
    }
}
