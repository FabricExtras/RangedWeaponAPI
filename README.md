# RangedWeaponAPI

### 🏹 Making bows and crossbows has never been easier!

## Features

### Ranged weapons
- [x] Bows and Crossbow construction
- [x] Customizable weapon properties: damage, pull time, projectile velocity (optional)
- [x] Automatic item model predicate registration (matching vanilla model predicates)
- [x] Tooltip includes weapon properties
- [x] Correct rendering first and third person rendering
- [x] Correct pull FOV
- [x] AI can use these weapons
  - Skeletons (+ Strays, Bogged, Illusioners) use custom bows, Pillagers and Piglins use custom crossbows — including custom pull time, damage scaling and correct arm poses
  - Applies to any weapon carrying `ranged_weapon:properties` (also third-party ones)
  - Mobs don't spawn with custom weapons by themselves — equip them via `/summon` HandItems, datapacks or content mods

![Example](.github/custom_longbow.png)

### Properties component

ID: `ranged_weapon:properties`

Weapon-physics properties of a ranged weapon.
- Presence of this component opts an item into the systems of this mod
- Vanilla bows and crossbows receive defaults automatically
- Third-party bows/crossbows can be integrated by attaching this component (no Java integration needed)

| Field | Type | Required | Description |
|---|---|---|---|
| `pull_time` | int (ticks) | yes | Pull time of the weapon. Definitive value — the `ranged_weapon:pull_time` attribute is display-only. |
| `damage` | float | no | Damage baseline: divisor for converting the shooter's `ranged_weapon:damage` attribute value into a projectile damage multiplier. Fallback when absent: 6 (bows), 9 (crossbows). Physics property, not a balance knob — must equal the vanilla-equivalent full charge output of the weapon type. Set only for weapon types with non-standard projectile physics. |
| `velocity` | float | no | Launch velocity baseline: divisor for converting the shooter's `ranged_weapon:velocity` attribute value into a projectile speed multiplier. Fallback when absent: 3.0 (bows), 3.15 (crossbows). Physics property, coupled to `damage`: `damage baseline = arrow base damage (2) × velocity baseline`. Set only for weapon types with non-standard launch velocity. |

How properties and attributes overlay:
- Properties act as the baseline: `damage` = damage multiplier divisor, `pull_time` = actual pull time
- Attribute modifiers act as bonuses on top: `ranged_weapon:damage` stacks from all sources (weapon, gear, effects)
- At shoot time: `projectile damage multiplier = damage attribute value ÷ properties damage`

#### Example — vanilla bow with modified pull time:

```
/give @p minecraft:bow[ranged_weapon:properties={pull_time:30}]
```

#### Example — a Longbow:
- properties: `{pull_time: 30}` → 1.5 sec pull time, damage baseline falls back to 6
- attributes: +12 Ranged Damage
- multiplier: `12 ÷ 6 = 2×` → deals 12 damage at full charge with a standard arrow

#### Example — integrating a third-party weapon:

Goal of the integration: a bow/crossbow from another mod does not participate in the systems of this mod (its ad-hoc subclass ignores the Ranged Damage attribute, so bonuses from gear, skill trees or effects do nothing for it). Stamping the component onto it makes it a first-class citizen: attribute bonuses scale its projectiles correctly, and its pull time becomes configurable.

Say `examplemod:heavy_bow` launches its arrows at velocity 4.5 (instead of the standard 3.0). Both baselines derive from that physics fact — `damage baseline = arrow base damage (2) × 4.5 = 9`:

- properties: `{damage: 9, velocity: 4.5, pull_time: 25}`
- a shooter with 3 bonus Ranged Damage (attribute value `9 + 3 = 12`) → multiplier `12 ÷ 9 = 1.33×`
- a shooter with 1.5 bonus Velocity → speed multiplier `(4.5 + 1.5) ÷ 4.5 = 1.33×`

Stamp the component as a default component (modpacks can do the same per-stack via loot/recipe functions):

```java
// Fabric (Fabric API), NeoForge equivalent: ModifyDefaultComponentsEvent
DefaultItemComponentEvents.MODIFY.register(context -> {
    context.modify(heavyBow, builder -> builder.add(
        RangedWeaponProperties.TYPE,
        new RangedWeaponProperties(Optional.of(9F), Optional.of(4.5F), 25)
    ));
});
```

Note: also give the item a `ranged_weapon:damage` attribute modifier (value = its full charge damage), so the weapon contributes its own base to the attribute stack and displays it on the tooltip.



### Attributes

- [x] Ranged Damage attribute
  - ID: `ranged_weapon:damage`
  - Specifies the damage amount of projectiles, randomized by the same relative amount as vanilla weapons, critical strike mechanic applies the same way
- [x] Pull Time attribute
  - ID: `ranged_weapon:pull_time`
  - DISPLAY-ONLY: renders authentic tooltip lines, the actual pull time is defined by the `ranged_weapon:properties` component
- [x] Draw Speed attribute 
  - ID: `ranged_weapon:haste`
  - Base value = 100
  - Example values: 50, -50% attack speed 
  - Example values: 200, +100% attack speed

`/give @p minecraft:leather_helmet[attribute_modifiers={modifiers:[{type:"ranged_weapon:haste",amount:0.2,slot:head,id:"armor_head_bonus",operation:add_multiplied_base}]}]`

![attribute_haste.png](.github/attribute_haste.png)

Each attribute has a corresponding status effect.

`/effect give @p ranged_weapon:damage`

![status_effect_damage.png](.github/status_effect_damage.png)

`/effect give @p ranged_weapon:haste`

![status_effect_haste.png](.github/status_effect_haste.png)

### Enchantments

This mod overrides the vanilla Power enchantment definition (`data/minecraft/enchantment/power.json`), to make it compatible with the attribute driven damage system.
- Vanilla behaviour: flat bonus added to arrow damage on impact — bypasses the `ranged_weapon:damage` attribute, so it doesn't scale with weapon damage
- Overridden behaviour: `+8% ranged_weapon:damage` per level (`add_multiplied_base`, mainhand) — scales correctly with any weapon integrated with this mod
- Datapacks can override this definition, as usual

## Try it out

Check out this repo, resolve dependencies, select `Testmod client` run config.

![Run config](.github/testmod_config.png)

Use `/give @p testmod:custom_longbow` to get a test bow.

## Installation

Add this mod as dependency into your build.gradle file.

Repository
```groovy
repositories {
    maven {
        name = 'Modrinth'
        url = 'https://api.modrinth.com/maven'
        content {
            includeGroup 'maven.modrinth'
        }
    }
}
```

Dependency
```groovy
dependencies {
    modImplementation "maven.modrinth:ranged-weapon-api:${project.ranged_weapon_api_version}"
}
```

## Usage

1. Create your bow/crossbow instance
2. Configure it
3. Register it
4. Add model and texture files
5. Done!

```java
var bow = new CustomBow(
    new Item.Settings().maxDamage(300),
    new RangedWeaponConfig(12, 30),   // 12 damage at full charge, 1.5 sec pull time
    () -> Ingredient.ofItems(Items.GOLD_INGOT)
);
Registry.register(
    Registries.ITEM,
    Identifier.of(NAMESPACE, "custom_longbow"), 
    bow
);
```

Check out the [example mod](src/testmod/java/net/testmod/TestMod.java).

## Include or depend

Feel free to include this API in your mod, the license allows you to do so.

If you want to say thank you, you can link this project as a dependency on Modrinth/CruseForge, so downloads are counted for this project too.

Mod ID: `ranged_weapon_api`

Modrinth dependency (gradle entry): `required.project 'ranged-weapon-api'`

CurseForge dependency (gradle entry): `requiredDependency 'ranged-weapon-api'`