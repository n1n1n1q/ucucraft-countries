package com.ucucraft.countries.config;

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
}
