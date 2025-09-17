package net.fabric_extras.ranged_weapon.internal;

public interface RangedHasteEntity {
    void resetPartialHasteTicks();
    float getPartialHasteTick();
    void addPartialHasteTick(float tick);
}
