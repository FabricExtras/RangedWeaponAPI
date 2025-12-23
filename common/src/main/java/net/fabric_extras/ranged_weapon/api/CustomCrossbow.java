package net.fabric_extras.ranged_weapon.api;

import net.fabric_extras.ranged_weapon.internal.RangedItemSettings;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;

import java.util.HashSet;

public class CustomCrossbow extends CrossbowItem {
    // Instances are kept a list of, so model predicates can be automatically registered
    public final static HashSet<CustomCrossbow> instances = new HashSet<>();

    private final TagKey<Item> repairTag;

    public CustomCrossbow(Settings settings, RangedConfig config, TagKey<Item> repairTag) {
        super(
                ((RangedItemSettings)settings).rangedAttributes(config).repairable(repairTag)
        );
        this.repairTag = repairTag;
        instances.add(this);
    }

    public TagKey<Item> getRepairTag() {
        return repairTag;
    }
}
