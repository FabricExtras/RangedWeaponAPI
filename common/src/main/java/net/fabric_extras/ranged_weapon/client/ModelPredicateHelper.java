package net.fabric_extras.ranged_weapon.client;

import net.fabric_extras.ranged_weapon.api.CustomCrossbow;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabric_extras.ranged_weapon.mixin.client.ModelPredicateProviderRegistryInvoker;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

/// 1.20.1 deltas vs 2.3.4: `ModelPredicateProviderRegistry.get(...)` takes an `Item`, not an `ItemStack`,
/// and `ItemStack#getMaxUseTime()` is no-arg. `register` is still private, so the invoker mixin stays.
public class ModelPredicateHelper {
    public static void registerBowModelPredicates(Item bow) {
        // We cannot reuse what is already registered for Vanilla bow, because it uses hardcoded pull time values
        ModelPredicateProviderRegistryInvoker.rwa_invokeRegister(bow, new Identifier("pull"), (stack, world, entity, seed) -> {
            if (entity == null) {
                return 0.0F;
            }
            if (entity.getActiveItem() != stack) {
                return 0.0F;
            }
            var pullTimeTicks = (float) entity.getAttributeValue(EntityAttributes_RangedWeapon.PULL_TIME.attribute) * 20F;
            if (pullTimeTicks <= 0) {
                return 0.0F;
            }
            return (float) (stack.getMaxUseTime() - entity.getItemUseTimeLeft()) / pullTimeTicks;
        });
        ModelPredicateProviderRegistryInvoker.rwa_invokeRegister(bow, new Identifier("pulling"), (stack, world, entity, seed) ->
                entity != null && entity.isUsingItem() && entity.getActiveItem() == stack ? 1.0F : 0.0F);
    }

    public static void registerCrossbowModelPredicates(CustomCrossbow crossbow) {
        var predicatesToCopy = new Identifier[] {
                new Identifier("pull"),
                new Identifier("pulling"),
                new Identifier("charged"),
                new Identifier("firework")
        };
        for (var predicateId : predicatesToCopy) {
            var predicateProvider = ModelPredicateProviderRegistry.get(Items.CROSSBOW, predicateId);
            if (predicateProvider == null) {
                continue;
            }
            ModelPredicateProviderRegistryInvoker.rwa_invokeRegister(crossbow, predicateId,
                    (stack, world, entity, seed) -> predicateProvider.call(stack, world, entity, seed));
        }
    }
}
