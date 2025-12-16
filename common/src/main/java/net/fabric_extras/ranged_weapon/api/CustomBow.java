package net.fabric_extras.ranged_weapon.api;

import net.fabric_extras.ranged_weapon.internal.RangedItemSettings;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;

public class CustomBow extends BowItem {
    // Instances are kept a list of, so model predicates can be automatically registered
    public final static HashSet<CustomBow> instances = new HashSet<>();

    private final @Nullable Item repairItem;
    private final @Nullable TagKey<Item> repairTag;

    public CustomBow(Settings settings, RangedConfig config, @Nullable Item repairItem) {
        super(
                ((RangedItemSettings)settings).rangedAttributes(config).repairable(repairItem)
        );
        this.repairItem = repairItem;
        this.repairTag = null;
        instances.add(this);
    }

    public CustomBow(Settings settings, RangedConfig config, @Nullable TagKey<Item> repairTag) {
        super(
                ((RangedItemSettings)settings).rangedAttributes(config).repairable(repairTag)
        );
        this.repairItem = null;
        this.repairTag = repairTag;
        instances.add(this);
    }

    public @Nullable Item getRepairItem() {
        return repairItem;
    }

    public @Nullable TagKey<Item> getRepairTag() {
        return repairTag;
    }
}

