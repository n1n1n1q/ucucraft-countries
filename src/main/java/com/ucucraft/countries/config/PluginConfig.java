package com.ucucraft.countries.config;

import java.util.List;

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
        return cfg().getInt("country.name-max-length", 16);
    }

    public String namePattern() {
        return cfg().getString("country.name-pattern", "^[A-Za-z0-9_]+$");
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

    public boolean placeholdersEnabled() {
        return cfg().getBoolean("placeholders.enabled", true);
    }

    /** Placeholder identifier, i.e. the "country" in %country_name%. */
    public String placeholderIdentifier() {
        return cfg().getString("placeholders.identifier", "country");
    }
}
