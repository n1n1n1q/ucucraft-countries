package com.ucucraft.countries.claim;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.ucucraft.countries.api.ChunkPos;

public final class ClaimStorage {

    private final Plugin plugin;
    private final File file;

    public ClaimStorage(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "claims.yml");
    }

    public Map<UUID, Set<ChunkPos>> loadAll() {
        Map<UUID, Set<ChunkPos>> result = new LinkedHashMap<>();
        if (!file.exists()) {
            return result;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = yaml.getConfigurationSection("claims");
        if (root == null) {
            return result;
        }
        for (String id : root.getKeys(false)) {
            UUID countryId;
            try {
                countryId = UUID.fromString(id);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Skipping claims for invalid country id '" + id + "'");
                continue;
            }
            Set<ChunkPos> chunks = new LinkedHashSet<>();
            for (String raw : root.getStringList(id)) {
                ChunkPos pos = ChunkPos.parse(raw);
                if (pos == null) {
                    plugin.getLogger().warning("Skipping malformed claim '" + raw + "'");
                    continue;
                }
                chunks.add(pos);
            }
            if (!chunks.isEmpty()) {
                result.put(countryId, chunks);
            }
        }
        return result;
    }

    public void saveAll(Map<UUID, Set<ChunkPos>> claims) {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, Set<ChunkPos>> entry : claims.entrySet()) {
            List<String> keys = new ArrayList<>();
            for (ChunkPos pos : entry.getValue()) {
                keys.add(pos.key());
            }
            yaml.set("claims." + entry.getKey(), keys);
        }
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save claims.yml", e);
        }
    }
}
