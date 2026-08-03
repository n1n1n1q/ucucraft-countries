package com.ucucraft.countries;

import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import com.ucucraft.countries.api.CountriesAPI;
import com.ucucraft.countries.api.impl.CountriesAPIImpl;
import com.ucucraft.countries.claim.ClaimManager;
import com.ucucraft.countries.claim.ClaimProtectionListener;
import com.ucucraft.countries.claim.ClaimStorage;
import com.ucucraft.countries.command.AcceptCommand;
import com.ucucraft.countries.command.AdminCommand;
import com.ucucraft.countries.command.CountryCommand;
import com.ucucraft.countries.config.Messages;
import com.ucucraft.countries.config.PluginConfig;
import com.ucucraft.countries.era.EraGateListener;
import com.ucucraft.countries.hook.CountryPlaceholders;
import com.ucucraft.countries.hook.DynmapHook;
import com.ucucraft.countries.era.EraManager;
import com.ucucraft.countries.era.EraRegistry;
import com.ucucraft.countries.manager.CountryManager;
import com.ucucraft.countries.manager.DiplomacyManager;
import com.ucucraft.countries.manager.InviteManager;
import com.ucucraft.countries.storage.CountryStorage;
import com.ucucraft.countries.vault.VaultListener;
import com.ucucraft.countries.vault.VaultManager;
import com.ucucraft.countries.vault.VaultStorage;

public final class CountriesPlugin extends JavaPlugin {

    private CountryManager countries;
    private InviteManager invites;
    private DiplomacyManager diplomacy;
    private VaultManager vaults;
    private ClaimManager claims;
    private CountryPlaceholders placeholders;
    private DynmapHook dynmap;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        PluginConfig config = new PluginConfig(this);
        Messages messages = new Messages(this);
        messages.load(config.language(), config.prefix());
        Messages eraMessages = new Messages(this, "lang/eras");
        eraMessages.load(config.language(), config.eraPrefix());

        countries = new CountryManager(config, new CountryStorage(this));
        countries.load();
        invites = new InviteManager(config);
        diplomacy = new DiplomacyManager(config);

        vaults = new VaultManager(config, new VaultStorage(this));
        vaults.load();

        EraRegistry registry = new EraRegistry(this, vaults);
        registry.load();

        claims = new ClaimManager(config, new ClaimStorage(this), registry);
        claims.load();

        EraManager eras = new EraManager(registry, countries, eraMessages);

        Services services = new Services(this, config, messages, eraMessages,
                countries, invites, diplomacy, eras, vaults, claims);

        getServer().getServicesManager().register(CountriesAPI.class,
                new CountriesAPIImpl(services), this, ServicePriority.Normal);

        if (config.placeholdersEnabled() && getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholders = new CountryPlaceholders(services);
            placeholders.register();
        }

        VaultListener vaultListener = new VaultListener(services);
        getServer().getPluginManager().registerEvents(vaultListener, this);
        getServer().getPluginManager().registerEvents(new EraGateListener(services), this);
        getServer().getPluginManager().registerEvents(new ClaimProtectionListener(services), this);

        bind("country", new CountryCommand(services, vaultListener));
        bind("accept", new AcceptCommand(services));
        bind("countryadmin", new AdminCommand(services));

        long sweep = Math.max(1L, config.inviteSweepSeconds()) * 20L;
        getServer().getScheduler().runTaskTimer(this, () -> {
            invites.sweep();
            diplomacy.sweep();
        }, sweep, sweep);

        if (config.dynmapEnabled()) {
            DynmapHook hook = new DynmapHook(services);
            if (hook.enable()) {
                dynmap = hook;
                long dynmapInterval = Math.max(5L, config.dynmapUpdateIntervalSeconds()) * 20L;
                getServer().getScheduler().runTaskTimer(this, hook::update, 20L, dynmapInterval);
            } else {
                getLogger().info("Dynmap not found; skipping map integration.");
            }
        }
    }

    @Override
    public void onDisable() {
        if (placeholders != null) {
            placeholders.unregister();
        }
        if (dynmap != null) {
            dynmap.disable();
        }
        getServer().getServicesManager().unregisterAll(this);
        if (countries != null) {
            countries.save();
        }
        if (vaults != null) {
            vaults.save();
        }
        if (claims != null) {
            claims.save();
        }
        if (invites != null) {
            invites.clear();
        }
        if (diplomacy != null) {
            diplomacy.clear();
        }
    }

    private void bind(String name, TabExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command != null) {
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        } else {
            getLogger().warning("Command '" + name + "' is missing from plugin.yml");
        }
    }
}
