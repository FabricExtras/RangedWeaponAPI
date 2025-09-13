package net.fabric_extras.ranged_weapon.neoforge;

import net.fabric_extras.ranged_weapon.neoforge.internal.RangedAttribute;
import net.fabric_extras.ranged_weapon.Platform;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.neoforged.fml.ModList;

public class PlatformImpl {
    public static Platform.Type getPlatformType() {
        return Platform.Type.NEOFORGE;
    }

    public static class NeoForgeUtil implements Platform.Util {
        @Override
        public boolean isModLoaded(String modid) {
            return ModList.get().isLoaded(modid);
        }

        @Override
        public ClampedEntityAttribute makeAttribute(String translationKey, double fallback, double min, double max) {
            return new RangedAttribute(translationKey, fallback, min, max);
        }
    }
    private static final Platform.Util UTIL = new NeoForgeUtil();
    public static Platform.Util util() {
        return UTIL;
    }
}
