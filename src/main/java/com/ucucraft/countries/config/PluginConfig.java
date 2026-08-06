package com.ucucraft.countries.config;

import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public final class PluginConfig {

    private final Plugin plugin;

    public PluginConfig(Plugin plugin) {
        this.plugin = plugin;
    }

    private FileConfiguration cfg() {
        return plugin.getConfig();
    }

    public String language() {
        return cfg().getString("language", "en");
    }

    /** MiniMessage prefix prepended to plugin messages. */
    public String prefix() {
        return cfg().getString("prefix.default", "");
    }

    /** Prefix for era messages; falls back to {@link #prefix()}. */
    public String eraPrefix() {
        return cfg().getString("prefix.eras", prefix());
    }

    /** Prefix for path messages; falls back to {@link #prefix()}. */
    public String pathPrefix() {
        return cfg().getString("prefix.paths", prefix());
    }

    public long inviteDurationSeconds() {
        return cfg().getLong("invite.duration-seconds", 60);
    }

    public long inviteSweepSeconds() {
        return cfg().getLong("invite.sweep-seconds", 20);
    }

    public int nameMinLength() {
        return cfg().getInt("country.name-min-length", 3);
    }

    public int nameMaxLength() {
        return cfg().getInt("country.name-max-length", 24);
    }

    public String namePattern() {
        return cfg().getString("country.name-pattern", "^[A-Za-z0-9_ ]+$");
    }

    public boolean inviteLeaderOnly() {
        return cfg().getBoolean("country.invite-leader-only", true);
    }

    public int listPageSize() {
        return cfg().getInt("list.page-size", 10);
    }

    public long diplomacyDurationSeconds() {
        return cfg().getLong("diplomacy.invite-duration-seconds", 60);
    }

    public boolean announce(String event) {
        return cfg().getBoolean("announce." + event, true);
    }

    public int vaultPages() {
        return Math.max(1, cfg().getInt("vault.pages", 3));
    }

    public int vaultRows() {
        return Math.min(5, Math.max(1, cfg().getInt("vault.rows", 5)));
    }

    /** Base chunk allowance; 0 or less means unlimited. */
    public int claimBaseLimit() {
        return cfg().getInt("claim.base-limit", 16);
    }

    public int claimPerEraBonus() {
        return cfg().getInt("claim.per-member-bonus", 4);
    }

    /** If true, only the leader may claim or unclaim land; otherwise leader and trusted members. */
    public boolean claimLeaderOnly() {
        return cfg().getBoolean("claim.leader-only", true);
    }

    public boolean claimRequireAdjacent() {
        return cfg().getBoolean("claim.require-adjacent", true);
    }

    public List<String> claimDisabledWorlds() {
        return cfg().getStringList("claim.disabled-worlds");
    }

    public boolean claimProtectionEnabled() {
        return cfg().getBoolean("claim.protection.enabled", true);
    }

    public boolean claimProtectionBuild() {
        return cfg().getBoolean("claim.protection.build", true);
    }

    public boolean claimProtectionContainers() {
        return cfg().getBoolean("claim.protection.containers", true);
    }

    public boolean claimProtectionBuckets() {
        return cfg().getBoolean("claim.protection.buckets", true);
    }

    public boolean claimProtectionExplosions() {
        return cfg().getBoolean("claim.protection.explosions", true);
    }

    public boolean claimProtectionFire() {
        return cfg().getBoolean("claim.protection.fire", true);
    }

    public boolean claimProtectionMobGriefing() {
        return cfg().getBoolean("claim.protection.mob-griefing", true);
    }

    public boolean claimPvpEnabled() {
        return cfg().getBoolean("claim.protection.pvp.enabled", true);
    }

    /** war-only, always-blocked, or always-allowed. */
    public String claimPvpMode() {
        return cfg().getString("claim.protection.pvp.mode", "war-only");
    }

    public boolean claimPvpFriendlyFire() {
        return cfg().getBoolean("claim.protection.pvp.friendly-fire", false);
    }

    /** Hand claimed land to the Titles plugin so it can show location titles. */
    public boolean titlesEnabled() {
        return cfg().getBoolean("titles.enabled", true);
    }

    /** Style section the Titles plugin renders claimed land with. */
    public String titleStyle() {
        return cfg().getString("titles.style", "country");
    }

    public int titlePriority() {
        return cfg().getInt("titles.priority", 0);
    }

    /** Show a title when leaving claimed land for unclaimed land. */
    public boolean titlesWilderness() {
        return cfg().getBoolean("titles.wilderness.enabled", true);
    }

    public String titleWildernessStyle() {
        return cfg().getString("titles.wilderness.style", "wilderness");
    }

    public int titleWildernessPriority() {
        return cfg().getInt("titles.wilderness.priority", -100);
    }

    /** Sound played on entering that country's land; empty when it has none. */
    public String titleSound(String country) {
        String own = cfg().getString("titles.sounds.by-country." + country);
        if (own == null) {
            for (String key : soundKeys()) {
                if (key.equalsIgnoreCase(country)) {
                    own = cfg().getString("titles.sounds.by-country." + key);
                    break;
                }
            }
        }
        return own != null ? own : cfg().getString("titles.sounds.default", "");
    }

    public String titleWildernessSound() {
        return cfg().getString("titles.wilderness.sound", "");
    }

    public float titleSoundVolume() {
        return (float) cfg().getDouble("titles.sounds.volume", 1.0);
    }

    public float titleSoundPitch() {
        return (float) cfg().getDouble("titles.sounds.pitch", 1.0);
    }

    private Set<String> soundKeys() {
        ConfigurationSection section = cfg().getConfigurationSection("titles.sounds.by-country");
        return section != null ? section.getKeys(false) : Set.of();
    }

    public boolean placeholdersEnabled() {
        return cfg().getBoolean("placeholders.enabled", true);
    }

    /** Placeholder identifier, i.e. the "country" in %country_name%. */
    public String placeholderIdentifier() {
        return cfg().getString("placeholders.identifier", "country");
    }

    public boolean dynmapEnabled() {
        return cfg().getBoolean("dynmap.enabled", true);
    }

    public String dynmapMarkerSetId() {
        return cfg().getString("dynmap.markerset-id", "countries.claims");
    }

    public String dynmapMarkerSetLabel() {
        return cfg().getString("dynmap.markerset-label", "Countries");
    }

    public long dynmapUpdateIntervalSeconds() {
        return cfg().getLong("dynmap.update-interval-seconds", 30);
    }

    /** Marker hover label; {country} is replaced with the country's name. */
    public String dynmapLabelFormat() {
        return cfg().getString("dynmap.label-format", "{country}");
    }

    public int dynmapLineWeight() {
        return cfg().getInt("dynmap.style.line-weight", 2);
    }

    public double dynmapLineOpacity() {
        return cfg().getDouble("dynmap.style.line-opacity", 0.8);
    }

    public double dynmapFillOpacity() {
        return cfg().getDouble("dynmap.style.fill-opacity", 0.35);
    }
}
