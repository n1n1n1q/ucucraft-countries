package com.ucucraft.countries;

import org.bukkit.plugin.Plugin;

import com.ucucraft.countries.config.Messages;
import com.ucucraft.countries.config.PluginConfig;
import com.ucucraft.countries.era.EraManager;
import com.ucucraft.countries.manager.CountryManager;
import com.ucucraft.countries.manager.DiplomacyManager;
import com.ucucraft.countries.manager.InviteManager;
import com.ucucraft.countries.vault.VaultManager;

public final class Services {

    public final Plugin plugin;
    public final PluginConfig config;
    public final Messages messages;
    public final Messages eraMessages;
    public final CountryManager countries;
    public final InviteManager invites;
    public final DiplomacyManager diplomacy;
    public final EraManager eras;
    public final VaultManager vaults;

    public Services(Plugin plugin, PluginConfig config, Messages messages, Messages eraMessages,
                    CountryManager countries, InviteManager invites, DiplomacyManager diplomacy,
                    EraManager eras, VaultManager vaults) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        this.eraMessages = eraMessages;
        this.countries = countries;
        this.invites = invites;
        this.diplomacy = diplomacy;
        this.eras = eras;
        this.vaults = vaults;
    }
}
