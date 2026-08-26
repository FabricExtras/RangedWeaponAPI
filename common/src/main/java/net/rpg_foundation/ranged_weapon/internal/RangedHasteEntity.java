package net.rpg_foundation.ranged_weapon.internal;

public interface RangedHasteEntity {
    void resetPartialHasteTicks();
    float getPartialHasteTick();
    void addPartialHasteTick(float tick);
}
