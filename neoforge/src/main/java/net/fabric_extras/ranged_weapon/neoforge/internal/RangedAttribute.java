package net.fabric_extras.ranged_weapon.neoforge.internal;

import net.fabric_extras.ranged_weapon.internal.NeoAttribute;
import net.minecraft.entity.attribute.ClampedEntityAttribute;
import net.minecraft.util.Identifier;
import net.neoforged.neoforge.common.extensions.IAttributeExtension;
import org.jetbrains.annotations.Nullable;

public class RangedAttribute extends ClampedEntityAttribute implements IAttributeExtension, NeoAttribute {
    public RangedAttribute(String translationKey, double fallback, double min, double max) {
        super(translationKey, fallback, min, max);
    }

    private Identifier baseModifierId = null;

    @Override
    public void setBaseModifierId(Identifier id) {
        baseModifierId = id;
    }

    @Nullable
    public Identifier getBaseId() {
        return baseModifierId;
    }
}
