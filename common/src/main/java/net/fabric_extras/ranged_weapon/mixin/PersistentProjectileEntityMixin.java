package net.fabric_extras.ranged_weapon.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabric_extras.ranged_weapon.api.BowMechanics;
import net.fabric_extras.ranged_weapon.internal.ArrowExtension;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Random;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin implements ArrowExtension {
    @Unique private static final Random RWA_CRIT_RANDOM = new Random();
    @Shadow private double damage;
    @Shadow public abstract boolean isCritical();

    /**
     * This math magic fixes incorrectly scaling damage when critical strikes occur
     * by eliminating flat additions.
     * <p>
     * Vanilla 1.20.1 `onEntityHit` computes `int i = ceil(clamp(velocity * damage, ...))` and then, for a
     * crit, adds `random.nextInt(i / 2 + 2)` — a flat bonus that does not scale with a boosted weapon.
     * `ordinal = 0` is that `i` (the first `int` local in the method); it is stored twice (once on
     * compute, once after the crit bonus) and the handler is idempotent, so both stores land on the
     * same multiplicative result.
     */
    @ModifyVariable(method = "onEntityHit", at = @At("STORE"), ordinal = 0)
    private int rwa_modifyCritDamage(int value) {
        if (!isCritical()) { return value; }
        var projectile = (PersistentProjectileEntity) ((Object) this);
        var velocity = projectile.getVelocity().length();
        var critMultiplier = 1F + (0.1F + RWA_CRIT_RANDOM.nextFloat() * 0.5F);
        return (int) Math.round(MathHelper.clamp(velocity * this.damage * critMultiplier, 0.0, 2.147483647E9));
    }

    /**
     * Percentage-based Power on the **mob** path — see {@link BowMechanics.Power}.
     * <p>
     * `applyEnchantmentEffects` is what skeletons/pillagers go through (players go through
     * `BowItem#onStoppedUsing`, hooked in `item.BowItemMixin`). It calls `setDamage` twice: `ordinal = 0`
     * seeds the base damage from the difficulty modifier, `ordinal = 1` is vanilla's Power block
     * (`setDamage(getDamage() + level * 0.5 + 0.5)`, guarded by `level > 0`). Only the second is wrapped,
     * and the seeded base is scaled instead of receiving the flat bonus.
     * <p>
     * Identical in the vanilla and Forge-patched class (Forge only shifts the line numbers).
     */
    @WrapOperation(
            method = "applyEnchantmentEffects",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/projectile/PersistentProjectileEntity;setDamage(D)V",
                    ordinal = 1
            )
    )
    private void rwa_applyPercentagePower(
            // Mixin parameters
            PersistentProjectileEntity instance, double vanillaDamage, Operation<Void> original,
            // Context parameters
            LivingEntity shooter, float damageModifier) {
        original.call(instance, instance.getDamage() * BowMechanics.Power.damageMultiplierOf(shooter));
    }

    @Unique private boolean rwa_modified = false;
    public void rwa_markModified(boolean modified) {
        this.rwa_modified = modified;
    }
    public boolean rwa_isModified() {
        return this.rwa_modified;
    }
}
