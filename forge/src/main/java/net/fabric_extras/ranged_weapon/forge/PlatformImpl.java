package net.fabric_extras.ranged_weapon.forge;

import net.fabric_extras.ranged_weapon.Platform;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraftforge.fml.ModList;

public class PlatformImpl {
    public static Platform.Type getPlatformType() {
        return Platform.Type.FORGE;
    }

    public static class ForgeUtil implements Platform.Util {
        @Override
        public boolean isModLoaded(String modid) {
            return ModList.get().isLoaded(modid);
        }

        @Override
        public ClampedEntityAttribute makeAttribute(String translationKey, double fallback, double min, double max) {
            // 1.20.1 delta: no `IAttributeExtension`-based tooltip hook (that is NeoForge's, and it exists
            // only because 1.21.1 renders attributes off data components). A plain clamped attribute is
            // what both loaders use here.
            return new ClampedEntityAttribute(translationKey, fallback, min, max);
        }
    }
    private static final Platform.Util UTIL = new ForgeUtil();
    public static Platform.Util util() {
        return UTIL;
    }
}
