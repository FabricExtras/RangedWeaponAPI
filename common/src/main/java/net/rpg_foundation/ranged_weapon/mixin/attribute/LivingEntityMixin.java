package net.rpg_foundation.ranged_weapon.mixin.attribute;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.rpg_foundation.ranged_weapon.Platform;
import net.rpg_foundation.ranged_weapon.api.EntityAttributes_RangedWeapon;
import net.rpg_foundation.ranged_weapon.internal.RangedHasteEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements RangedHasteEntity {
    LivingEntityMixin(final EntityType<?> type, final Level world) {
        super(type, world);
    }

    @Inject(
            method = "createLivingAttributes()Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier$Builder;",
            require = 1, allow = 1, at = @At("RETURN")
    )
    private static void addAttributes(final CallbackInfoReturnable<AttributeSupplier.Builder> info) {
        for (var entry : EntityAttributes_RangedWeapon.all) {
            info.getReturnValue().add(entry.entry);
        }
    }

    @Shadow protected int useItemRemaining;
    @Shadow protected ItemStack useItem;

    @Inject(method = "getUseItemRemainingTicks", at = @At("HEAD"), cancellable = true)
    private void getItemUseTimeLeft(CallbackInfoReturnable<Integer> info) {
        if (Platform.NeoForge) {
            var useAction = useItem.getUseAnimation();
            if (useAction == ItemUseAnimation.BOW || useAction == ItemUseAnimation.CROSSBOW) {
                // Make sure the partial tick integer cast happens BEFORE subtracting
                info.setReturnValue(useItemRemaining - (int)partialHasteTick);
            }
        } else {
            var value = useItemRemaining;
            var entity = (LivingEntity) (Object) this;
            if (entity.isUsingItem()) {
                var useAction = useItem.getUseAnimation();
                if (useAction == ItemUseAnimation.BOW || useAction == ItemUseAnimation.CROSSBOW) {
                    var progress = useItem.getUseDuration(entity) - value;
                    var haste = entity.getAttributeValue(EntityAttributes_RangedWeapon.HASTE.entry);
                    var newProgress = (int) (progress * EntityAttributes_RangedWeapon.HASTE.asMultiplier((float) haste));
                    info.setReturnValue(useItem.getUseDuration(entity) - newProgress);
                }
            }
        }
    }

    @Inject(method = "stopUsingItem", at = @At("TAIL"))
    private void clearActiveItem_RWA(CallbackInfo ci) {
        this.resetPartialHasteTicks();
    }

    @Unique private float partialHasteTick = 0f;
    public void addPartialHasteTick(float tick) {
        this.partialHasteTick += tick;
    }
    public void resetPartialHasteTicks() {
        this.partialHasteTick = 0f;
    }
    public float getPartialHasteTick() {
        return this.partialHasteTick;
    }
}
