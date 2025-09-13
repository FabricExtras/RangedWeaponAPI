package net.fabric_extras.ranged_weapon.client;

import net.fabric_extras.ranged_weapon.api.CustomCrossbow;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class ModelPredicateHelper {
    public static void registerBowModelPredicates(Item bow) {
        // We cannot reuse what is already registered for Vanilla bow, because it uses hardcoded pull time values
        ModelPredicateProviderRegistry.register(bow, Identifier.of("pull"), (stack, world, entity, seed) -> {
            if (entity == null) {
                return 0.0F;
            } else {
                return entity.getActiveItem() != stack ? 0.0F : (float)(stack.getMaxUseTime(entity) - entity.getItemUseTimeLeft()) / ( (float) entity.getAttributeValue(EntityAttributes_RangedWeapon.PULL_TIME.entry) * 20F);
            }
        });
        ModelPredicateProviderRegistry.register(bow, Identifier.of("pulling"), (stack, world, entity, seed) -> {
            return entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F;
        });
    }

    public static void registerCrossbowModelPredicates(CustomCrossbow crossbow) {
        var predicatesToCopy = new Identifier[] {
                Identifier.of("pull"),
                Identifier.of("pulling"),
                Identifier.of("charged"),
                Identifier.of("firework")
        };
        for (var predicateId : predicatesToCopy) {
            var predicateProvider = ModelPredicateProviderRegistry.get(Items.CROSSBOW.getDefaultStack(), predicateId);
            ModelPredicateProviderRegistry.register(crossbow, predicateId, (stack, world, entity, seed) -> {
                return predicateProvider.call(stack, world, entity, seed);
            });
        }
    }
}