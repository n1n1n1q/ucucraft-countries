# `config.yml` — configuration reference

Everything the country layer itself reads. Era progression lives in `eras.yml` (documented by
its own header comments) and paths in `paths.yml` ([PATHS.md](PATHS.md)); message texts live in
the language files.

| File | Holds |
| --- | --- |
| `config.yml` | Everything on this page. |
| `eras.yml` | The era ladder, requirements, material gating, per-era chunk limits. |
| `paths.yml` | Paths, tiers, buffs — see [PATHS.md](PATHS.md). |
| `lang/<language>.yml` | Country, claim, diplomacy and title texts. |
| `lang/eras/<language>.yml` | Era, vault and admin texts. |
| `lang/paths/<language>.yml` | Path and ability texts. |

Keys added by a newer plugin version are merged into an existing `config.yml` on startup, so
upgrading never loses comments-free defaults. Missing keys always fall back to the defaults
listed below.

`/countryadmin reload` re-reads `config.yml`, `eras.yml`, `paths.yml` and every language file.
Values read once at startup need a **restart**: the invite sweep interval, the dynmap update
interval, the path buff interval, and whether the PlaceholderAPI / dynmap / Titles hooks are
registered at all.

## Language and prefixes

```yaml
language: en
prefix:
  default: "<gray>[<aqua>Countries</aqua>]</gray> "
  eras: "<gray>[<gold>Eras</gold>]</gray> "
  paths: "<gray>[<light_purple>Paths</light_purple>]</gray> "
```

| Key | Default | Meaning |
| --- | --- | --- |
| `language` | `en` | Which `lang/<language>.yml` (and `lang/eras/`, `lang/paths/`) to load. Falls back to `en.yml` when the file is missing. |
| `prefix.default` | `[Countries]` | MiniMessage prefix on country messages. Empty string = none. |
| `prefix.eras` | `[Eras]` | Prefix for era, vault and admin messages; falls back to `prefix.default`. |
| `prefix.paths` | `[Paths]` | Prefix for path and ability messages; falls back to `prefix.default`. |

Prefixes apply to `send`/`broadcast` messages, not to list headers and info lines, so tables
stay clean.

## Country names and membership

```yaml
country:
  name-min-length: 3
  name-max-length: 24
  name-pattern: "^[A-Za-z0-9_ ]+$"
  invite-leader-only: true
```

| Key | Default | Meaning |
| --- | --- | --- |
| `country.name-min-length` | `3` | Rejected below this with `name-too-short`. |
| `country.name-max-length` | `24` | Rejected above this with `name-too-long`. |
| `country.name-pattern` | `^[A-Za-z0-9_ ]+$` | Java regex the whole name must match. The space is what allows multi-word names; drop it to force single words. |
| `country.invite-leader-only` | `true` | `false` lets any member invite. |

Names are unique case-insensitively. Multi-word names work in every command and tab-complete
word by word; quotes (`/country info "Mali Nalisniki"`) are accepted but not required.

## Invites

```yaml
invite:
  duration-seconds: 60
  sweep-seconds: 20
```

| Key | Default | Meaning |
| --- | --- | --- |
| `invite.duration-seconds` | `60` | How long `/accept <player>` stays possible. |
| `invite.sweep-seconds` | `20` | How often expired invites and diplomacy offers are purged. Restart to change. |

Invites live in memory only — a restart drops them.

## Diplomacy

```yaml
diplomacy:
  invite-duration-seconds: 60
```

| Key | Default | Meaning |
| --- | --- | --- |
| `diplomacy.invite-duration-seconds` | `60` | Lifetime of a pending alliance invite or peace offer. |

Wars and alliances themselves are permanent until `/country peace` or `/country ally disband`;
declaring war on an ally breaks the alliance automatically.

## Announcements

```yaml
announce:
  created: true
  disbanded: true
  renamed: true
  alliance: true
  war: true
  peace: true
```

Each flag switches one server-wide broadcast. Setting it to `false` does not silence the
event — the acting player still gets a private confirmation instead.

Era ascension broadcasts are separate (`settings.broadcast` in `eras.yml`), as are path
choice and ability broadcasts (`settings.announce-choice` in `paths.yml`, `broadcast` per
ability).

## Country list

```yaml
list:
  page-size: 10
```

| Key | Default | Meaning |
| --- | --- | --- |
| `list.page-size` | `10` | Entries per page in `/country list`, also used for `/country era list`. |

## Vault

```yaml
vault:
  pages: 3
  rows: 5
```

| Key | Default | Meaning |
| --- | --- | --- |
| `vault.pages` | `3` | Vault pages per country. Minimum 1. |
| `vault.rows` | `5` | Content rows per page, clamped to 1–5; a navigation row is added below them. |

Total slots are `pages × rows × 9`. **Shrinking either value drops the items stored in the
slots that disappear** — increase freely, decrease only on an empty vault.

The vault is what era `resource` requirements are paid from, and path `era-cost` buffs
discount those requirements.

## Land claims

```yaml
claim:
  base-limit: 16
  per-member-bonus: 4
  leader-only: true
  require-adjacent: true
  disabled-worlds: []
```

| Key | Default | Meaning |
| --- | --- | --- |
| `claim.base-limit` | `16` | Fallback chunk allowance. `0` or less = unlimited. |
| `claim.per-member-bonus` | `4` | Fallback extra chunks per member. |
| `claim.leader-only` | `true` | `false` lets trusted members claim and unclaim too. |
| `claim.require-adjacent` | `true` | Every claim after the first must touch land you already own. |
| `claim.disabled-worlds` | `[]` | World names where claiming is refused. |

**Allowance formula.** The era wins when it defines a limit:

```
era.base-chunk-limit >= 0  ->  era.base-chunk-limit + era.additional-chunks-per-player × members
otherwise                  ->  claim.base-limit      + claim.per-member-bonus          × members
plus the path claim-limit buffs (flat + per-member × members)
```

A base of `0` on either side means unlimited, and then path bonuses no longer matter. The
shipped `eras.yml` defines a limit for every era, so the `claim.*` numbers act as a safety net
for eras that leave `base-chunk-limit: -1`.

### Protection

```yaml
claim:
  protection:
    enabled: true
    build: true
    containers: true
    buckets: true
    explosions: true
    fire: true
    mob-griefing: true
```

| Key | Default | Blocks outsiders from |
| --- | --- | --- |
| `protection.enabled` | `true` | Master switch for everything below. |
| `protection.build` | `true` | Breaking/placing blocks, and breaking paintings, item frames and armor stands. |
| `protection.containers` | `true` | Opening containers and using doors, trapdoors, gates, buttons, plates, levers. |
| `protection.buckets` | `true` | Placing or collecting water and lava. |
| `protection.explosions` | `true` | Any explosion damaging claimed blocks (creepers, TNT, beds). |
| `protection.fire` | `true` | Igniting, burning and fire spreading onto claimed land. |
| `protection.mob-griefing` | `true` | Mobs changing claimed blocks (endermen, ravagers, sheep). |

Members of the owning country are never restricted, and `countries.admin.bypassclaim`
(op by default) ignores protection entirely. Explosions, fire and mob griefing are blocked on
claimed land regardless of who triggered them.

### PvP

```yaml
claim:
  protection:
    pvp:
      enabled: true
      mode: war-only
      friendly-fire: false
```

| Key | Default | Meaning |
| --- | --- | --- |
| `pvp.enabled` | `true` | `false` = claims never restrict PvP. |
| `pvp.mode` | `war-only` | `war-only` — only countries at war may fight on claimed land; `always-blocked` — no PvP on claimed land; `always-allowed` — claims never block PvP. |
| `pvp.friendly-fire` | `false` | Whether members of the same country may fight on their own land. |

Only the **defender's** chunk matters, and unclaimed land is never restricted. Path `damage`
buffs apply on top of whatever survives these rules.

## Location titles

Requires the separate `Titles` plugin; without it this section is ignored and a note is logged
at startup.

```yaml
titles:
  enabled: true
  style: country
  priority: 0
  sounds:
    default: ""
    volume: 1.0
    pitch: 1.0
    by-country: {}
  wilderness:
    enabled: true
    style: wilderness
    priority: -100
    sound: ""
```

| Key | Default | Meaning |
| --- | --- | --- |
| `titles.enabled` | `true` | Hand claimed land to the Titles plugin at all. |
| `titles.style` | `country` | Which style section Titles renders country land with. |
| `titles.priority` | `0` | Wins over another plugin's area with a lower priority on the same spot. |
| `titles.sounds.default` | `""` | Sound key played on entering a country with no entry of its own; empty = silent. |
| `titles.sounds.volume` / `.pitch` | `1.0` | Playback of those sounds. |
| `titles.sounds.by-country` | `{}` | `Country Name: "sound:key"` map, matched ignoring case. Vanilla keys work as-is; custom ones need a resource pack. |
| `titles.wilderness.enabled` | `true` | Show a title when leaving claimed land. |
| `titles.wilderness.style` | `wilderness` | Style section for open land. |
| `titles.wilderness.priority` | `-100` | Negative, so a named place from another plugin wins. |
| `titles.wilderness.sound` | `""` | Sound key for wilderness; empty = silent. |

The texts are `title-country`, `subtitle-country`, `title-wilderness`, `subtitle-wilderness` in
`lang/<language>.yml`. Animations, timings, the repeat cooldown and `show-on-join` belong to
the Titles plugin (`plugins/Titles/config.yml`), not here.

## PlaceholderAPI

```yaml
placeholders:
  enabled: true
  identifier: country
```

| Key | Default | Meaning |
| --- | --- | --- |
| `placeholders.enabled` | `true` | Register the expansion when PlaceholderAPI is installed. Restart to change. |
| `placeholders.identifier` | `country` | Namespace: `country` yields `%country%`, `%country_era%`, `%country_path%`, … |

## Dynmap

```yaml
dynmap:
  enabled: true
  markerset-id: "countries.claims"
  markerset-label: "Countries"
  update-interval-seconds: 30
  label-format: "{country}"
  style:
    line-weight: 2
    line-opacity: 0.8
    fill-opacity: 0.35
```

| Key | Default | Meaning |
| --- | --- | --- |
| `dynmap.enabled` | `true` | Draw claims on the web map when Dynmap is installed. Restart to change. |
| `dynmap.markerset-id` | `countries.claims` | Marker set id; also the key Dynmap stores its own per-set settings under. |
| `dynmap.markerset-label` | `Countries` | Layer name in the map's layer control. |
| `dynmap.update-interval-seconds` | `30` | Redraw period, minimum 5. Restart to change. |
| `dynmap.label-format` | `{country}` | Hover label; `{country}` is the country's name. |
| `dynmap.style.line-weight` | `2` | Border thickness. |
| `dynmap.style.line-opacity` | `0.8` | Border opacity. |
| `dynmap.style.fill-opacity` | `0.35` | Fill opacity. |

## Storage files

| File | Contents |
| --- | --- |
| `countries.yml` | One block per country: id, name, leader, members, trusted, allies, wars, completed criteria, `era`, `era-since`, `path`, `path-since`, `ability-cooldowns`. |
| `claims.yml` | Claimed chunks per country id. |
| `vaults.yml` | Vault contents per country id. |

All three are written on every relevant change and on shutdown, so editing them by hand is
only safe while the server is stopped. Invites and diplomacy offers are memory-only.
