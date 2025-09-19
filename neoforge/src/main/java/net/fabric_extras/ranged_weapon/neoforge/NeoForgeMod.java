package net.fabric_extras.ranged_weapon.neoforge;

import net.fabric_extras.ranged_weapon.RangedWeaponMod;
import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.fabric_extras.ranged_weapon.internal.RangedHasteEntity;
import net.minecraft.util.UseAction;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

@Mod(RangedWeaponMod.ID)
public final class NeoForgeMod {
    public NeoForgeMod() {
        RangedWeaponMod.init();
        
        // Reset happens via mixin
        // `clearActiveItem`

//        NeoForge.EVENT_BUS.addListener(LivingEntityUseItemEvent.Start.class, (event) -> {
//            var entity = event.getEntity();
//            ((RangedHasteEntity)entity).resetPartialHasteTicks();
//        });

//        NeoForge.EVENT_BUS.addListener(LivingEntityUseItemEvent.Stop.class, (event) -> {
//            var entity = event.getEntity();
//            ((RangedHasteEntity)entity).resetPartialHasteTicks();
//        });

//        NeoForge.EVENT_BUS.addListener(LivingEntityUseItemEvent.Finish.class, (event) -> {
//            var entity = event.getEntity();
//            ((RangedHasteEntity)entity).resetPartialHasteTicks();
//        });

        NeoForge.EVENT_BUS.addListener(LivingEntityUseItemEvent.Tick.class, (event) -> {
            var entity = event.getEntity();
            var activeItemStack = entity.getActiveItem();
            if (entity.isUsingItem())  {
                var useAction = activeItemStack.getUseAction();
                if (useAction == UseAction.BOW || useAction == UseAction.CROSSBOW) {
                    var haste = entity.getAttributeValue(EntityAttributes_RangedWeapon.HASTE.entry);
                    if (haste != EntityAttributes_RangedWeapon.HASTE.baseValue) {
                        // Upon calling this event, NeoForge modifies the itemUseTimeLeft already
                        // by querying it, and than setting it back to itself.
                        // Hence we step back by one partial tick
                        event.setDuration((int) (event.getDuration() + ((RangedHasteEntity)entity).getPartialHasteTick()));

                        var time = entity.getAttributeValue(EntityAttributes_RangedWeapon.PULL_TIME.entry);
                        // var timeTicks = Math.round(time * 20);
                        var bonus = EntityAttributes_RangedWeapon.HASTE.asMultiplier(haste) - 1F;
                        var partialTick = time * bonus;
                        ((RangedHasteEntity)entity).addPartialHasteTick((float) partialTick);
                    }
                }
            }
        });
    }
}
