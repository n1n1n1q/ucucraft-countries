# Building Dynmap with Paper 26.x support

Upstream Dynmap (`webbukkit/dynmap`) does not support Paper 26.x yet — its
NMS helper layer doesn't recognize 26.x internals and the plugin disables
itself on load. A community fork adds a `paper-helper-26x` module built
against Paper's real 26.x mappings via paperweight userdev. This doc
covers building that fork yourself.

Fork used: `taranovegor/dynmap`, branch `feat/paper-26.1`
(fork of `webbukkit/dynmap`, real dev, coherent commit history — reviewed
before building).

## Option A: use the already-built jar

A verified build already exists at:
```
/Users/anya/UCUCRAFT/Dynmap-3.9-SNAPSHOT-spigot.jar
```
Built from the fork branch above, tested live on Paper 26.2 (see
`## Deploying` / `## Rendering` below for how to install and confirm it).
Just copy it into your server's `plugins/` folder — skip straight to
"Deploying".

Note: the fork has no GitHub Releases or CI artifacts of its own, so
there's no official "download" link for newer commits — if the branch
moves forward, Option B is the only way to get an updated jar. Don't
substitute a jar from somewhere else (e.g. a PR comment attachment) —
those have no verifiable provenance; anyone can attach a file to a
comment. This local jar is trustworthy specifically because it was
compiled here from a reviewed source branch, not downloaded.

## Option B: build from source

Use this if the fork has new commits, you need a different Paper 26.x
build number, or you just don't trust a jar you didn't compile yourself
(reasonable default).

## Requirements

- JDK 25 (the `paper-helper-26x` module compiles with
  `sourceCompatibility/targetCompatibility = VERSION_25`)
- Network access (first build pulls a full Paper 26.x mapped dev bundle
  via paperweight, plus dependency jars for every legacy MC version
  Dynmap still supports — expect a large download on first run)

Check available JDKs:
```
/usr/libexec/java_home -V
```

If Gradle's own launcher is pinned elsewhere (e.g. via
`~/.gradle/gradle.properties` → `org.gradle.java.home`), override it
per-command with `-Dorg.gradle.java.home=...` rather than changing the
global setting — the launcher JDK and the project's toolchain are
independent knobs.

### Steps

1. Clone the branch:
   ```
   git clone --branch feat/paper-26.1 --single-branch \
     https://github.com/taranovegor/dynmap.git
   cd dynmap
   ```

2. Build the Paper 26.x helper module (validates against real Paper
   26.x mappings via paperweight):
   ```
   ./gradlew -Dorg.gradle.java.home=<path-to-jdk25> \
     :paper-helper-26x:build --console=plain
   ```
   First run takes a while (downloads vanilla server jar, runs
   codebook/mache remapping, applies Paperclip + dev bundle patches).

3. Build the full plugin jar (spigot module bundles every version
   helper, including `paper-helper-26x`):
   ```
   ./gradlew -Dorg.gradle.java.home=<path-to-jdk25> \
     :spigot:shadowJar --console=plain
   ```
   Output: `target/Dynmap-<version>-spigot.jar`

   Known flake: this module also fetches mapped jars for old
   Spigot/Bukkit versions (1.13 through 1.21) from `repo.mikeprimm.com`.
   A slow/timed-out fetch fails the whole build with a `Read timed out`
   — just rerun the same command, Gradle resumes from cache.

4. Verify the jar is the right one before deploying:
   ```
   unzip -p target/Dynmap-*-spigot.jar plugin.yml
   ```
   Confirms `main: org.dynmap.bukkit.DynmapPlugin` and the version
   string.

## Deploying

Drop the jar into your server's `plugins/` folder and restart (Dynmap
only loads at boot — hot-dropping the jar into a running server's
`plugins/` dir does nothing until restart).

Confirm it actually enabled on 26.x by checking the log for:
```
[dynmap] version=26.x-...-... (MC: 26.x)
...
[dynmap] Enabled
```
No `Enabled` line, or an `unsupported platform` message, means the
fork's version check didn't match your exact Paper build — check the
fork's commit log for newer patches.

## Rendering

Fresh worlds have no map tiles until chunks are visited or a full
render is triggered. In-game (needs op or `dynmap.*` perm) or console:
```
/dynmap fullrender <world>
/dynmap cancelrender      # abort an in-progress fullrender
/dynmap stats             # progress / tile-render throughput
```
Web UI defaults to `http://<server-ip>:8123`.
