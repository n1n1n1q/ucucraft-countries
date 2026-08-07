# Paths — configuration reference

A country's founder picks one path; the path grants **buffs** in **tiers**, and each tier is
unlocked by an era from `eras.yml`. Everything below lives in `plugins/Countries/paths.yml`
and is applied live with `/countryadmin reload` — adding, moving or retuning a path never
needs a code change.

Files involved:

| File | Holds |
| --- | --- |
| `paths.yml` | Paths, tiers, buffs, and the path settings block. |
| `lang/paths/<language>.yml` | Every path message shown to players. |
| `config.yml` → `prefix.paths` | Chat prefix for those messages (falls back to `prefix.default`). |
| `countries.yml` | Per-country `path`, `path-since`, `ability-cooldowns`. |

## File layout

```yaml
settings:
  ...                       # global switches, see below

paths:
  <path-id>:                # lowercase id used in commands
    display: "<red>Path of War</red>"
    description:            # shown by /path list and /path info
      - "<gray>One line of pitch."
    tiers:
      <era-id>:             # an id from eras.yml — this is the milestone
        display: "<gold>Foundation</gold>"
        description:
          - "<gray>What this tier gives, in words."
        effects:
          - type: attribute
            ...
```

A country with era index ≥ the tier's era index has that tier's buffs. Tiers are sorted by
era order automatically, so the key order in the file does not matter. A tier pointing at an
era id that does not exist is skipped with a warning in the server log.

## settings

| Key | Default | Meaning |
| --- | --- | --- |
| `enabled` | `true` | Master switch. `false` = no buffs, no listeners, no buff task; `/path` answers "disabled". |
| `leader-only` | `true` | Only the leader may run `/path choose`. |
| `allow-change` | `false` | `false` = the first choice is final; `true` = `/path choose` may switch paths any time. |
| `announce-choice` | `true` | Broadcast the choice server-wide instead of a private confirmation. |
| `announce-unlock` | `true` | Message every online citizen when an ascension unlocks a tier. |
| `effect-interval-ticks` | `40` | How often passive attribute/potion buffs are recomputed. Minimum 1; 20 ticks = 1s. |
| `potion.ambient` | `true` | Passive and ability potions use the "beacon" look. |
| `potion.particles` | `false` | Swirl particles on buffed players. |
| `potion.icon` | `true` | Show the effect icon in the HUD. |
| `underground-y` | `40` | Y level below which the `underground` scope is true. |

Lowering `effect-interval-ticks` makes scope changes (stepping on/off your land) snappier at
a small CPU cost; the value also drives the potion refresh length, so passive potions always
outlive one interval.

## Buff types

Every entry under a tier's `effects` needs a `type` and may carry a `scope` (next section).
Unknown types, attributes, potions, materials or damage causes are skipped with a warning —
the rest of the file still loads.

### `attribute`

```yaml
- type: attribute
  attribute: MAX_HEALTH        # any Bukkit attribute key
  amount: 4.0                  # 4.0 = 2 hearts
  operation: add_number        # add_number | add_scalar | multiply_scalar_1
  scope: own-claims
```

| Field | Default | Notes |
| --- | --- | --- |
| `attribute` | — | Registry key, e.g. `ATTACK_DAMAGE`, `ARMOR`, `MAX_HEALTH`, `ATTACK_SPEED`, `MOVEMENT_SPEED`, `SNEAKING_SPEED`, `KNOCKBACK_RESISTANCE`, `LUCK`, `BLOCK_BREAK_SPEED`. |
| `amount` | `0` | Amount `0` is treated as "no modifier". |
| `operation` | `add_number` | Vanilla semantics: flat, +% of base, ×. |

Applied as *transient* modifiers keyed `countries:path.<attribute>.<operation>`, so nothing is
left behind if the plugin is removed, and a player never accumulates duplicates.

### `potion`

```yaml
- type: potion
  potion: NIGHT_VISION
  amplifier: 0                 # 0 = level I
  scope: night
```

Kept refreshed while the scope holds and removed when it stops. A stronger or longer effect
from another source (a drunk potion, a beacon) is never overwritten.

### `damage`

```yaml
- type: damage
  direction: taken             # taken | dealt
  multiplier: 0.5
  causes: [ENTITY_EXPLOSION, BLOCK_EXPLOSION]   # omit for every cause
  scope: own-claims
```

`taken` scales damage the citizen receives, `dealt` scales damage they deal (melee and their
own projectiles). `causes` takes Bukkit `DamageCause` names.

### `drops`

```yaml
- type: drops
  multiplier: 1.25
  materials: [IRON_ORE, DEEPSLATE_IRON_ORE]     # omit for every block
```

`materials` filters on the **block that was broken**, not on the item dropped. Fractional
bonuses are a chance: `1.25` pays one extra item on roughly a quarter of the drops. Stacks
are never pushed past their maximum size.

### `claim-limit`

```yaml
- type: claim-limit
  flat: 4
  per-member: 1
```

Added on top of the era's chunk allowance: `flat + per-member × members`. Country-wide, so it
ignores `scope`. No effect while the allowance is unlimited (`claim.base-limit: 0`).

### `era-cost`

```yaml
- type: era-cost
  multiplier: 0.9              # 10% cheaper
```

Scales every `resource` requirement of the next era — `/country era` shows the discounted
numbers and ascension consumes the discounted amount. Country-wide, so it ignores `scope`.
Each item stays at 1 minimum.

### `ability`

```yaml
- type: ability
  id: warcry                   # /path use warcry
  display: "<dark_red>Warcry</dark_red>"
  target: country              # self | nearby | country
  radius: 24                   # nearby only, in blocks
  cooldown-seconds: 3600
  broadcast: true
  scope: at-war                # conditions the user must satisfy
  effects:
    - potion: STRENGTH
      amplifier: 1
      seconds: 120
```

| `target` | Who gets the potions |
| --- | --- |
| `self` | The player who ran the command. |
| `nearby` | Online citizens of the same country within `radius` (always includes the user). |
| `country` | Every online citizen, anywhere. |

Cooldowns are **per country**, not per player, and are stored in `countries.yml`, so they
survive restarts. Changing a country's path clears them. `broadcast: true` announces the use
server-wide (`ability-broadcast`); recipients other than the user get `ability-received`.

## Scopes

Any buff takes `scope`, either one condition or a list — **all** listed conditions must hold:

```yaml
scope: own-claims
scope: [foreign-claims, sneaking]
```

| Condition | True when |
| --- | --- |
| `always` | Always (the default when `scope` is omitted). |
| `own-claims` | Standing on land your country owns. |
| `foreign-claims` | Standing on land owned by any other country. |
| `enemy-claims` | Standing on land owned by a country you are at war with. |
| `any-claims` | Standing on any claimed land. |
| `wilderness` | Standing on unclaimed land. |
| `at-war` | Your country has at least one active war. |
| `at-peace` | Your country has no active wars. |
| `day` / `night` | World time outside / inside 13000–23000. |
| `sneaking` | The player is sneaking. |
| `underground` | Player Y is below `settings.underground-y`. |

`claim-limit` and `era-cost` are country-wide and ignore scopes.

## How buffs stack

Buffs from *all* unlocked tiers apply at once; nothing is replaced by a later tier.

| Type | Stacking |
| --- | --- |
| `attribute` | Summed per attribute and operation. |
| `potion` | Strongest amplifier wins. |
| `damage` | Multiplied (0.9 and 0.85 → 0.765). |
| `drops` | Multiplied. |
| `claim-limit` | Summed. |
| `era-cost` | Multiplied. |
| `ability` | Each id is its own ability with its own cooldown. |

So a "+4 chunks" tier followed by a "+6 chunks" tier yields +10, and the tier text should say
"+6 **more** chunks" to stay honest.

## Recipes

**Add a path.** Copy an existing block under `paths`, change the id, `display` and tier
contents, `/countryadmin reload`. It shows up in `/path list` immediately.

```yaml
  faith:
    display: "<light_purple>Path of Faith</light_purple>"
    description:
      - "<gray>Slow to fight, hard to break."
    tiers:
      copper-age:
        display: "<gold>Foundation</gold>"
        description:
          - "<gray>Regeneration on your own land"
        effects:
          - type: potion
            potion: REGENERATION
            amplifier: 0
            scope: own-claims
```

**Move a milestone.** Rename the tier key: `gold-age:` → `iron-age:`. Countries already past
that era get the tier at once.

**Change the cadence.** The shipped ladder unlocks on `copper-age`, `gold-age`,
`netherite-age` (every second era). For a tier per era, give each path a tier for every era
id; for two milestones only, delete one tier from each path.

**Retune.** Every number — `amount`, `multiplier`, `flat`, `per-member`, `radius`,
`cooldown-seconds`, `seconds`, `amplifier` — is a plain edit plus a reload.

**Turn paths off.** `settings.enabled: false`. Countries keep their stored path but get no
buffs, and `/path` reports that paths are disabled.

**Let countries switch paths.** `settings.allow-change: true`.

**Translate.** Copy `lang/paths/en.yml` to `lang/paths/<language>.yml` and set `language` in
`config.yml`. Path and tier `display`/`description` text stays in `paths.yml` (same convention
as era displays in `eras.yml`).

## Commands and permissions

| Command | Who |
| --- | --- |
| `/path`, `/path list`, `/path info <path>` | Anyone (`/country path ...` and `/cpath` are the same). |
| `/path choose <path>` | Leader, unless `settings.leader-only: false`. |
| `/path use <ability>` | Any citizen of the country. |
| `/countryadmin setpath <country> <path\|none>` | `countries.admin.setpath` |
| `/countryadmin reload` | `countries.admin.reload` — re-reads `eras.yml`, `paths.yml` and all language files. |

Country names with spaces work in every command and tab-complete word by word.

## Storage

```yaml
countries:
  mali nalisniki:
    ...
    path: war
    path-since: 1785938504802
    ability-cooldowns:
      warcry: 1785939001233
```

`path` is absent for a country that has not chosen — that is the default state for every new
country; no path is ever assigned automatically.

## Gotchas

- Tier keys must match era ids in `eras.yml` exactly (`gold-age`, not `golden-age`).
- Attribute and potion names are registry keys: `MAX_HEALTH` (not `GENERIC_MAX_HEALTH`),
  `HASTE` (not `FAST_DIGGING`), `STRENGTH` (not `INCREASE_DAMAGE`).
- The unlock announcement fires on a real ascension (`/country era advance` or an automatic
  one), not on `/countryadmin setera`.
- A `MAX_HEALTH` buff scoped to your own land drops the extra hearts the moment a player
  steps off it — expected, and the reason to keep such buffs modest.
- Warnings from a bad entry appear once at startup or reload in the server log, prefixed
  `[Countries]`; the rest of `paths.yml` still loads.
