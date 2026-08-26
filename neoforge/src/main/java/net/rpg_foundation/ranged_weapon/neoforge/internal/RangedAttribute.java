package net.rpg_foundation.ranged_weapon.neoforge.internal;

import net.rpg_foundation.ranged_weapon.internal.NeoAttribute;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.extensions.IAttributeExtension;
import org.jetbrains.annotations.Nullable;

public class RangedAttribute extends net.minecraft.world.entity.ai.attributes.RangedAttribute implements IAttributeExtension, NeoAttribute {
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
