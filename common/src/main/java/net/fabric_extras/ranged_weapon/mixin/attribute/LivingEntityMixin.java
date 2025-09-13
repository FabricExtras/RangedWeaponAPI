package net.fabric_extras.ranged_weapon.mixin.attribute;

import net.fabric_extras.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    LivingEntityMixin(final EntityType<?> type, final World world) {
        super(type, world);
    }

    @Inject(
            method = "createLivingAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;",
            require = 1, allow = 1, at = @At("RETURN")
    )
    private static void addAttributes(final CallbackInfoReturnable<DefaultAttributeContainer.Builder> info) {
        for (var entry : EntityAttributes_RangedWeapon.all) {
            info.getReturnValue().add(entry.entry);
        }
    }

    @Shadow protected int itemUseTimeLeft;
    @Shadow protected ItemStack activeItemStack;

    @Inject(method = "getItemUseTimeLeft", at = @At("HEAD"), cancellable = true)
    private void getItemUseTimeLeft(CallbackInfoReturnable<Integer> info) {
        var value  = itemUseTimeLeft;
        var entity = (LivingEntity)(Object)this;
        if (entity.isUsingItem())  {
            var useAction = activeItemStack.getUseAction();
            if (useAction == UseAction.BOW || useAction == UseAction.CROSSBOW) {
                var progress = activeItemStack.getMaxUseTime(entity) - value;
                var haste = entity.getAttributeValue(EntityAttributes_RangedWeapon.HASTE.entry);
                var newProgress = (int) (progress * EntityAttributes_RangedWeapon.HASTE.asMultiplier((float) haste));
                info.setReturnValue(activeItemStack.getMaxUseTime(entity) - newProgress);
            }
        }
    }
}
