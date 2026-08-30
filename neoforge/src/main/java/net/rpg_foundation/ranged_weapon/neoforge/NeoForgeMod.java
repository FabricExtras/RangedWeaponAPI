package net.rpg_foundation.ranged_weapon.neoforge;

import net.rpg_foundation.ranged_weapon.RangedWeaponMod;
import net.rpg_foundation.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.rpg_foundation.ranged_weapon.api.RangedWeaponProperties;
import net.rpg_foundation.ranged_weapon.internal.RangedHasteEntity;
import net.minecraft.world.item.ItemUseAnimation;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

@Mod(RangedWeaponMod.ID)
public final class NeoForgeMod {
    public NeoForgeMod() {
        RangedWeaponMod.init();

        // The accumulator is reset by `attribute/LivingEntityMixin#clearActiveItem_RWA` (`stopUsingItem` TAIL),
        // not by a `LivingEntityUseItemEvent.Start/Stop/Finish` listener.
        NeoForge.EVENT_BUS.addListener(LivingEntityUseItemEvent.Tick.class, (event) -> {
            var entity = event.getEntity();
            var activeItemStack = entity.getUseItem();
            if (entity.isUsingItem())  {
                var useAction = activeItemStack.getUseAnimation();
                if (useAction == ItemUseAnimation.BOW || useAction == ItemUseAnimation.CROSSBOW) {
                    var haste = entity.getAttributeValue(EntityAttributes_RangedWeapon.HASTE.entry);
                    if (haste != EntityAttributes_RangedWeapon.HASTE.baseValue) {
                        // Upon calling this event, NeoForge modifies the itemUseTimeLeft already
                        // by querying it, and than setting it back to itself.
                        // Hence we step back by one partial tick
                        event.setDuration((int) (event.getDuration() + ((RangedHasteEntity)entity).getPartialHasteTick()));

                        var time = RangedWeaponProperties.pullTimeTicks(activeItemStack, 20) / 20F;
                        var bonus = EntityAttributes_RangedWeapon.HASTE.asMultiplier(haste) - 1F;
                        var partialTick = time * bonus;
                        ((RangedHasteEntity)entity).addPartialHasteTick((float) partialTick);
                    }
                }
            }
        });
    }
}
