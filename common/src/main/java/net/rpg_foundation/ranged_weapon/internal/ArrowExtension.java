package net.rpg_foundation.ranged_weapon.internal;

public interface ArrowExtension {
    void rwa_markModified(boolean modified);
    boolean rwa_isModified();

    /**
     * The base damage of the arrow (the `damage` field). Vanilla removed the public getter in 1.21.2.
     */
    double rwa_getDamage();
}
