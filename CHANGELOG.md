# 2.3.3

- Fix ranged weapon damage dealt by Bow using mobs (such as Skeletons, Illusioners, etc.)

# 2.3.2

- Add EMI repair support

# 2.3.1

- Add some safeguard for mod loading compat on NeoForge

# 2.3.0

- Extend RangedConfig with optional list of attribute modifiers

# 2.2.1

- Stacking Attribute Modifier components

# 2.2.0

- Migrate to Architectury
- Fix NeoForge attribute tooltips

# 2.1.1

- Add effect description support

# 2.1.0

- Clean up attribute modifier slot usage of ranged weapons, thanks to Muon #15 

# 2.0.6

- Fine tune ranged critical strike damage multiplication
- Update translations

# 2.0.5

- Update some translations
- Add function for potion registration

# 2.0.4

- Lower Fabric API version requirement

# 2.0.3

- Allow running on 1.21

# 2.0.2

- Remove console spam

# 2.0.1

- Update to Minecraft 1.21.1

# 2.0.0

- Update to Minecraft 1.21
- Add new attributes, previously stored internally within item instances, now fully adjustable via item attribute component:
  - `ranged_weapon:pull_time`
  - `ranged_weapon:haste`

# 1.1.3

- Add Spanish translation
- Update Korean translation

# 1.1.2

- Update Fabric Loader to 15+ for embedded MixinExtras
- Add Italian translation, thanks to Zano

# 1.1.1

API Changes:
- BREAKING! - CustomRangedWeapon - Remove internally used get functions
- Removed dependency on Projectile Damage Attribute

Functional changes:
- Add new attributes:
  - `ranged_weapon:damage`
  - `ranged_weapon:haste`

# 1.0.0

Initial release.

#