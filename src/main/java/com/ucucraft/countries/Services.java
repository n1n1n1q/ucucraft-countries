package com.ucucraft.countries;

import org.bukkit.plugin.Plugin;

import com.ucucraft.countries.config.Messages;
import com.ucucraft.countries.config.PluginConfig;
import com.ucucraft.countries.manager.CountryManager;
import com.ucucraft.countries.manager.InviteManager;

public final class Services {

    public final Plugin plugin;
    public final PluginConfig config;
    public final Messages messages;
    public final CountryManager countries;
    public final InviteManager invites;

    public Services(Plugin plugin, PluginConfig config, Messages messages,
                    CountryManager countries, InviteManager invites) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        this.countries = countries;
        this.invites = invites;
    }
}
