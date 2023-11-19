package net.fabric_extras.ranged_weapon.api;

public interface CustomRangedWeapon {
    int getCustomPullTime_RPGS();
    void setCustomPullTime_RPGS(int pullTime);
    float getCustomVelocity_RPGS();
    void setCustomVelocity_RPGS(float velocity);
    float getCustomPullProgress(int useTicks);

    default void configure(RangedWeaponConfig config) {
        var rangedWeapon = (CustomRangedWeapon)this;
        rangedWeapon.setCustomPullTime_RPGS(config.pull_time());
        if (config.velocity() != null) {
            float velocity = config.velocity();
            rangedWeapon.setCustomVelocity_RPGS(velocity);
//            ((IProjectileWeapon)this).setCustomLaunchVelocity((double) velocity);
        } else {
            rangedWeapon.setCustomVelocity_RPGS(0F);
//            ((IProjectileWeapon)this).setCustomLaunchVelocity(null);
        }
//        ((IProjectileWeapon)this).setProjectileDamage(config.damage());
    }
}
