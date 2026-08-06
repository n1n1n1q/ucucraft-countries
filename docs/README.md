# Countries

A Paper (26.2) plugin for creating and managing countries.

## Commands

| Command | Description |
| --- | --- |
| `/country create <name>` | Found a new country (you become the leader). |
| `/country invite <player>` | Invite an online, country-less player (valid 60s). |
| `/accept <player>` | Accept a pending invite from that inviter. |
| `/country leave` | Leave your country (leaders must disband instead). |
| `/country disband` | Disband your country (leader only). |
| `/country rename <name>` | Rename your country (leader only). |
| `/country info [name]` | Show name, leader, members and land. |
| `/country list [page]` | Browse all countries, 10 per page. |
| `/country era` | Show era progress and requirements. |
| `/path` | Show your path, its tiers and abilities. |
| `/path list` | List every path with a short pitch. |
| `/path info <path>` | Show all tiers and buffs of a path. |
| `/path choose <path>` | Pick the country's path (leader only, final by default). |
| `/path use <ability>` | Fire an unlocked path ability. |
| `/country claim` | Claim the chunk you are standing in. |
| `/country unclaim` | Release the chunk you are standing in. |

`/c` is an alias for `/country`, `/cpath` for `/path`, and every `/path ...` form also
works as `/country path ...`. Country names may contain spaces and tab-complete word by word.

Admin: `/countryadmin <complete|revoke|setera|setpath|reload> ...`.

## Configuration

- `config.yml` — language, message prefixes, invite and diplomacy timings, name rules,
  announcements, page size, vault size, claim limits and protection, titles, placeholders,
  dynmap (full reference: [CONFIG.md](CONFIG.md)).
- `eras.yml` — the era ladder, its requirements and material gating.
- `paths.yml` — the paths, the eras that unlock their tiers and every buff number
  (full reference: [PATHS.md](PATHS.md)).
- `lang/<language>.yml`, `lang/eras/<language>.yml`, `lang/paths/<language>.yml` —
  every player-facing message (MiniMessage formatted).

Countries persist to `countries.yml`, land to `claims.yml`; invites live in memory only.

### Land claims

A country's chunk allowance comes from its era when that era defines one
(`base-chunk-limit + additional-chunks-per-player × members` in `eras.yml`), otherwise from
`claim.base-limit + claim.per-member-bonus × members` in `config.yml`; path `claim-limit`
buffs are added on top. A base limit of `0` means unlimited. With `claim.require-adjacent`
enabled the first claim can go anywhere and every later one must touch existing land.

Claimed land is protected out of the box (`claim.protection.*`: building, containers,
buckets, explosions, fire, mob griefing, and PvP in `war-only` mode by default) and the same
data is exposed through the API — use `CountriesAPI#canBuild` from other plugins.

### Paths

The founder picks one path for the country (`/path choose <path>`); by default the choice is
final (`settings.allow-change` in `paths.yml`). A path grants buffs in tiers, and a tier is
keyed by an era id from `eras.yml` — the shipped cadence is every second era, so buffs land on
Copper (Foundation), Golden (Growth) and Netherite (Capstone) while Stone, Iron and Diamond
stay buff-free. Move a milestone by renaming a tier key to another era id.

New countries have **no path** until the leader picks one; nothing is assigned automatically.

Five paths ship by default: War, Shadows, Industry, Trade and Diplomacy. Adding a sixth is a
`paths.yml` edit — no code. Each tier lists `effects`, and every effect type is generic:

| Type | Effect |
| --- | --- |
| `attribute` | Any Bukkit attribute (attack damage, armor, max health, sneaking speed, ...). |
| `potion` | A potion effect kept up while the scope holds. |
| `damage` | Multiplies damage dealt or taken, optionally only for listed damage causes. |
| `drops` | Multiplies block yields, optionally only for listed blocks. |
| `claim-limit` | Extra chunks, flat and per member. |
| `era-cost` | Multiplies the resource requirements of the next era. |
| `ability` | Activated buff on a cooldown, fired with `/path use <id>`. |

Every effect takes an optional `scope` — one condition or a list that must all hold:
`always`, `own-claims`, `foreign-claims`, `enemy-claims`, `any-claims`, `wilderness`,
`at-war`, `at-peace`, `day`, `night`, `sneaking`, `underground`. That is how "+2 hearts on
your own land" or "invisible while sneaking on foreign soil" are expressed without code.

Admins can override a choice with `/countryadmin setpath <country> <path|none>`;
`/countryadmin reload` re-reads `paths.yml` live.

**Full configuration reference: [PATHS.md](PATHS.md)** — every setting, every effect field and
its default, stacking rules, and recipes for adding a path or moving a milestone.

### Location titles

Entering another country's land shows its name across the screen, in the style of
Elden Ring / Hollow Knight area titles. The same happens on login for the land the
player is standing on (`show-on-join` in the Titles plugin's own config).

Requires the separate `Titles` plugin (`../ucucraft-titles`) in the server's `plugins/`
folder. Without it the rest of the plugin works and titles are skipped, with a note in
the startup log.

Titles is a general location-title plugin: this plugin only tells it that a chunk belongs
to a country, and Titles handles movement detection, the repeat cooldown (default 120s, so
pacing across a border does not spam) and the animation. Leaving claimed land shows the
wilderness title; disable it with `titles.wilderness.enabled: false`.

Texts live in `lang/<language>.yml` (`title-country`, `subtitle-country`,
`title-wilderness`, `subtitle-wilderness`). `config.yml` picks which style Titles renders
them with (`titles.style`, default `country`) and the priority used when another plugin
names the same spot. Animations, timings and the cooldown live in `plugins/Titles/config.yml`
— see `../ucucraft-titles/README.md`.

## PlaceholderAPI

Installed automatically when PlaceholderAPI is present. The namespace is
configurable via `placeholders.identifier` (default `country`).

| Placeholder | Value |
| --- | --- |
| `%country%` / `%country_name%` | Country name, or `placeholder-no-country`. |
| `%country_leader%` | Leader's name. |
| `%country_members%` | Member count. |
| `%country_member_names%` | Comma-separated member names. |
| `%country_allies%` / `%country_wars%` | Ally / enemy count. |
| `%country_era%` / `%country_era_index%` | Current era display / index. |
| `%country_path%` / `%country_path_id%` | Chosen path display / id. |
| `%country_path_tiers%` | Number of unlocked path tiers. |
| `%country_chunks%` / `%country_chunk_limit%` | Chunks owned / allowance. |
| `%country_here%` | Country owning the chunk the player stands in. |
| `%country_has_country%` | `true` / `false`. |

Output uses legacy colour codes, so it drops straight into tab, scoreboard and
chat plugins.

## API

Other plugins read country and chunk data through `CountriesAPI`. Add
`depend: [Countries]` to your `plugin.yml` and grab the service:

```java
CountriesAPI api = CountriesProvider.get();

api.getCountryOf(player).ifPresent(country ->
        getLogger().info(player.getName() + " leads " + country.claims().size() + " chunks"));

// Who owns the ground under the player?
String owner = api.getCountryAt(player.getLocation())
        .map(CountryView::name)
        .orElse("wilderness");

// Protection hook
if (!api.canBuild(player, event.getBlock().getLocation())) {
    event.setCancelled(true);
}

Relation relation = api.getRelation(attacker, victim); // SELF / ALLY / WAR / NEUTRAL
```

`CountriesProvider.get()` is a thin wrapper over the Bukkit `ServicesManager`, so
`Bukkit.getServicesManager().getRegistration(CountriesAPI.class)` works too.
Every `CountryView` collection is unmodifiable — the API is read-only.

## Building

Requires JDK 25 (the Gradle toolchain auto-provisions it if it isn't already installed).
`../ucucraft-titles` is a composite build, so that folder must sit next to this one; it
produces its own `Titles` plugin jar, which `./gradlew build` builds alongside this one and
`./gradlew runServer` installs into the test server automatically.

```
./gradlew build
```

The jar is produced at `build/libs/countries-<version>.jar`. Drop it into a
Paper server's `plugins/` folder, together with
`../ucucraft-titles/build/libs/ucucraft-titles-<version>.jar` if you want location titles.

### Note: non-ASCII project path

If the project path contains non-ASCII characters (e.g. Cyrillic), the
`gradlew` / `gradlew.bat` launcher scripts fail to resolve the wrapper
classpath. Build with a relative classpath instead:

```
java -cp gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain build
```

Or clone the project to an ASCII-only path.

## Testing on a live server

```
java -cp gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain runServer
```

`run-paper` downloads and launches Paper 26.2 with the plugin installed.
