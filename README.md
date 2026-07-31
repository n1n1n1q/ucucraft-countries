# Countries

A Paper (1.21.x) plugin for creating and managing countries.

## Commands

| Command | Description |
| --- | --- |
| `/country create <name>` | Found a new country (you become the leader). |
| `/country invite <player>` | Invite an online, country-less player (valid 60s). |
| `/accept <player>` | Accept a pending invite from that inviter. |
| `/country leave` | Leave your country (leaders must disband instead). |
| `/country disband` | Disband your country (leader only). |
| `/country rename <name>` | Rename your country (leader only). |
| `/country info [name]` | Show name, leader and members. |
| `/country list [page]` | Browse all countries, 10 per page. |

`/c` is an alias for `/country`.

## Configuration

- `config.yml` — invite duration/sweep, name rules, page size, language.
- `lang/<language>.yml` — every player-facing message (MiniMessage formatted).

Countries persist to `countries.yml`; invites live in memory only.

## Building

Requires JDK 21.

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

`run-paper` downloads and launches Paper 1.21.4 with the plugin installed.
