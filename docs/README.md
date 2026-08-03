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
| `/country claim` | Claim the chunk you are standing in. |
| `/country unclaim` | Release the chunk you are standing in. |

`/c` is an alias for `/country`.

## Configuration

- `config.yml` — message prefixes, invite duration/sweep, name rules, page size,
  claim limits, placeholder settings, language.
- `lang/<language>.yml` — every player-facing message (MiniMessage formatted).

Countries persist to `countries.yml`, land to `claims.yml`; invites live in memory only.

### Land claims

A country's chunk allowance is `claim.base-limit + claim.per-era-bonus * era`.
Set `base-limit` to `0` for unlimited. With `claim.require-adjacent` enabled the
first claim can go anywhere and every later one must touch existing land.

Claims are tracked and exposed through the API; this plugin does not itself block
building on foreign land — use `CountriesAPI#canBuild` from your protection plugin.

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

```
./gradlew build
```

The jar is produced at `build/libs/countries-<version>.jar`. Drop it into a
Paper server's `plugins/` folder.

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
