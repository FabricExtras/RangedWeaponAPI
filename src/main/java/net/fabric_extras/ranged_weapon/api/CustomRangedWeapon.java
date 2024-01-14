package net.fabric_extras.ranged_weapon.api;

import net.projectile_damage.api.IProjectileWeapon;

public interface CustomRangedWeapon {
    int getPullTime_RWA();
    void setPullTime_RWA(int pullTime);
    float getVelocity_RWA();
    void setVelocity_RWA(float velocity);
    float getPullProgress_RWA(int useTicks);

    default void configure(RangedConfig config) {
        var rangedWeapon = (CustomRangedWeapon)this;
        rangedWeapon.setPullTime_RWA(config.pull_time());
        if (config.velocity() != null) {
            float velocity = config.velocity();
            rangedWeapon.setVelocity_RWA(velocity);
            ((IProjectileWeapon)this).setCustomLaunchVelocity((double) velocity);
        } else {
            rangedWeapon.setVelocity_RWA(0F);
            ((IProjectileWeapon)this).setCustomLaunchVelocity(null);
        }
        ((IProjectileWeapon)this).setProjectileDamage(config.damage());
    }
}
