package com.ucucraft.countries.vault;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public final class VaultStorage {

    private final Plugin plugin;
    private final File file;

    public VaultStorage(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "vaults.yml");
    }

    public Map<String, CountryVault> loadAll(int size) {
        Map<String, CountryVault> result = new HashMap<>();
        if (!file.exists()) {
            return result;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = yaml.getConfigurationSection("vaults");
        if (root == null) {
            return result;
        }
        for (String id : root.getKeys(false)) {
            CountryVault vault = new CountryVault(size);
            List<?> raw = root.getList(id);
            if (raw != null) {
                for (int i = 0; i < raw.size() && i < size; i++) {
                    if (raw.get(i) instanceof ItemStack stack) {
                        vault.set(i, stack);
                    }
                }
            }
            result.put(id, vault);
        }
        return result;
    }

    public void saveAll(Map<String, CountryVault> vaults) {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<String, CountryVault> entry : vaults.entrySet()) {
            List<ItemStack> list = new ArrayList<>();
            for (ItemStack stack : entry.getValue().raw()) {
                list.add(stack);
            }
            yaml.set("vaults." + entry.getKey(), list);
        }
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save vaults.yml", e);
        }
    }
}
