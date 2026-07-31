package com.ucucraft.countries.storage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.ucucraft.countries.model.Country;

public final class CountryStorage {

    private final Plugin plugin;
    private final File file;

    public CountryStorage(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "countries.yml");
    }

    public List<Country> loadAll() {
        List<Country> result = new ArrayList<>();
        if (!file.exists()) {
            return result;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = yaml.getConfigurationSection("countries");
        if (root == null) {
            return result;
        }
        for (String key : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            String name = section.getString("name", key);
            String leaderRaw = section.getString("leader");
            if (leaderRaw == null) {
                continue;
            }
            Country country = new Country(name, UUID.fromString(leaderRaw));
            for (String member : section.getStringList("members")) {
                country.addMember(UUID.fromString(member));
            }
            result.add(country);
        }
        return result;
    }

    public void saveAll(Collection<Country> countries) {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Country country : countries) {
            String path = "countries." + country.key();
            yaml.set(path + ".name", country.getName());
            yaml.set(path + ".leader", country.getLeader().toString());
            List<String> members = new ArrayList<>();
            for (UUID uuid : country.getMembers()) {
                members.add(uuid.toString());
            }
            yaml.set(path + ".members", members);
        }
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save countries.yml", e);
        }
    }
}
