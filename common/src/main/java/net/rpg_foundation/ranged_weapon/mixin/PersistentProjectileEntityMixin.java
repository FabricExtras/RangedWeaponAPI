package net.rpg_foundation.ranged_weapon.mixin;

import net.rpg_foundation.ranged_weapon.internal.ArrowExtension;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Random;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileEntityMixin implements ArrowExtension {
    private static final Random CRIT_RANDOM = new Random();
    @Shadow private double damage;
    @Shadow public abstract boolean isCritical();

    /**
     * Replaces vanilla's critical-hit bonus with a multiplicative 1.1x - 1.6x roll.
     *
     * Vanilla `onEntityHit` computes `int i = ceil(clamp(velocity * damage, ...))` and then, when
     * the arrow is critical, adds a random flat bonus: `l = random.nextInt(i / 2 + 2); i = min(l + i, MAX)`.
     * That roll is *nominally* proportional to `i` (0 .. i/2 + 1), but the `+ 2` floor and the integer
     * truncation make it behave erratically at the high base damages custom bows reach, and its spread
     * (0% .. ~50%) is far wider than intended for this mod's balance. This handler discards it and
     * recomputes the damage as `velocity * damage * (1.1 .. 1.6)` instead.
     *
     * **The double match is load-bearing.** `i` is the first `int` local in the method, so
     * `@ModifyVariable(at = STORE, ordinal = 0)` fires at BOTH stores to it: the initial
     * `int i = ceil(...)` and the `i = min(l + i, ...)` inside the crit branch. The second one is the
     * one that decides the outcome - it is what overwrites (and thereby discards) vanilla's flat
     * bonus. Narrowing the injection to the first store only would leave vanilla's roll stacked on
     * top of our multiplier; narrowing it to the second alone would work today but breaks the moment
     * another `int` local is introduced ahead of `i`. On the non-critical path both invocations
     * return `value` untouched.
     *
     * Note: this deliberately recomputes from the shadowed `this.damage` field rather than the local
     * `d`, so any `EnchantmentHelper.getDamage` adjustment applied to `d` is not reflected on crits.
     * Long-standing behaviour - kept as is.
     */
    @ModifyVariable(method = "onEntityHit", at = @At("STORE"), ordinal = 0)
    private int modifyCritDamage(int value) {
        if (!isCritical()) { return value; }
        var projectile = (PersistentProjectileEntity) ((Object) this);
        var velocity = projectile.getVelocity().length();
        var critMultiplier = 1F + (0.1F + CRIT_RANDOM.nextFloat() * 0.5F);
        return (int) Math.round(MathHelper.clamp(velocity * this.damage * critMultiplier, 0.0, 2.147483647E9));
    }

    private boolean rwa_modified = false;
    public void rwa_markModified(boolean modified) {
        this.rwa_modified = modified;
    }
    public boolean rwa_isModified() {
        return this.rwa_modified;
    }
    public double rwa_getDamage() {
        return this.damage;
    }
}
