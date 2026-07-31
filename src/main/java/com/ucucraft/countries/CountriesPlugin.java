package com.ucucraft.countries;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import com.ucucraft.countries.command.AcceptCommand;
import com.ucucraft.countries.command.CountryCommand;
import com.ucucraft.countries.config.Messages;
import com.ucucraft.countries.config.PluginConfig;
import com.ucucraft.countries.manager.CountryManager;
import com.ucucraft.countries.manager.InviteManager;
import com.ucucraft.countries.storage.CountryStorage;

public final class CountriesPlugin extends JavaPlugin {

    private CountryManager countries;
    private InviteManager invites;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        PluginConfig config = new PluginConfig(this);
        Messages messages = new Messages(this);
        messages.load(config.language());

        CountryStorage storage = new CountryStorage(this);
        countries = new CountryManager(config, storage);
        countries.load();
        invites = new InviteManager(config);

        Services services = new Services(this, config, messages, countries, invites);

        bind("country", new CountryCommand(services));
        bind("accept", new AcceptCommand(services));

        long sweep = Math.max(1L, config.inviteSweepSeconds()) * 20L;
        getServer().getScheduler().runTaskTimer(this, invites::sweep, sweep, sweep);
    }

    @Override
    public void onDisable() {
        if (countries != null) {
            countries.save();
        }
        if (invites != null) {
            invites.clear();
        }
    }

    private void bind(String name, org.bukkit.command.TabExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        } else {
            getLogger().warning("Command '" + name + "' is missing from plugin.yml");
        }
    }
}
