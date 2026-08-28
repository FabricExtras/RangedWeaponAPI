# 4.0.0

Package and maven group reworked, no functional change.

- Minecraft 26.1.2 support (Java 25).
- Jars are no longer remapped: the shipped jar is the shaded platform jar
- `ranged_weapon:properties` and the config-derived attribute modifiers now bind at resource reload (components are empty until the first reload)
- Removed the `assets/minecraft/items/bow.json` override: vanilla's `use_duration` definition already animates custom pull times
- Java package is now `net.rpg_foundation.ranged_weapon` (was `net.fabric_extras.ranged_weapon`)
- Maven group is now `net.rpg_foundation` (was `net.foundation`), artifact `ranged_weapon_api-<loader>` unchanged
- Mod id, resource namespaces and every `ranged_weapon:` attribute/effect/component/property id are unchanged
- Migration: update imports and the Gradle coordinate
- BREAKING: `CustomBow`/`CustomCrossbow` no longer take a repair ingredient supplier; repairability is the vanilla `minecraft:repairable` component (`Item.Settings.repairable(tag)`)
- Ranged weapon item models use the vanilla properties: bows `minecraft:use_duration` (scale 0.05, re-expressed against the item's real pull time), crossbows `minecraft:crossbow/pull`; `ranged_weapon:pull` is deprecated
- Fix potion and tipped arrow names on 1.21.11
- Trimmed mob-AI injects that vanilla no longer reaches

# 3.0.0

Ranged weapon properties are now stored in the `ranged_weapon:properties` item component, instead of injected instance fields.

- New `ranged_weapon:properties` item component: `pull_time` (definitive pull time, in ticks), optional `damage` (weapon-physics damage baseline, falls back to 6 for bows and 9 for crossbows) and optional `velocity` (weapon-physics launch velocity baseline, falls back to 3.0 for bows and 3.15 for crossbows). Component presence is what opts an item into the ranged weapon systems; vanilla bows and crossbows receive defaults automatically. Third-party bows/crossbows can be integrated by attaching this component - no Java integration needed.
- BREAKING: the `ranged_weapon:pull_time` entity attribute is now display-only. It renders authentic tooltip lines but no longer participates in pull time calculation - attach the component instead to change pull time.
- BREAKING: removed `CustomRangedWeapon` (use the component's `damage` field instead of `setTypeBaseline`), `CrossbowMechanics`, `BowMechanics` and `TooltipUtil`.
- BREAKING: `RangedWeaponItem` subclasses other than bows and crossbows no longer receive damage scaling implicitly; they participate by carrying the component.
- New authoring type `RangedWeaponConfig` using absolute values: `damage` (full charge), `pull_time` (ticks), optional `velocity` bonus and optional attribute list. `CustomBow`/`CustomCrossbow` gained constructors taking it.
- DEPRECATED: `RangedConfig` (bonus-based values) and the constructors taking it. They keep working (converted via `RangedConfig.toAbsolute()`) but will be deleted in a future release — migrate to `RangedWeaponConfig`.
- Override the vanilla Power enchantment definition: instead of flat arrow damage (which bypasses the attribute system), Power now grants +8% `ranged_weapon:damage` per level.
- Mob AI can now use custom ranged weapons: Skeleton family + Illusioners fire custom bows (honoring their pull time), Pillagers and Piglins use custom crossbows, with correct arm poses. Works for any weapon carrying `ranged_weapon:properties`. (These mixins live in the `mixin/ai` package, so they can be temporarily disabled together during game version migrations.)

# 2.3.4

- Fully translated content, now supporting 20 languages
- Marked as library for Fabric
- Updated project icons

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