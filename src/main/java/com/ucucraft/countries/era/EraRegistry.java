package com.ucucraft.countries.era;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.ucucraft.countries.config.Sections;
import com.ucucraft.countries.era.requirement.Requirement;
import com.ucucraft.countries.era.requirement.RequirementFactory;
import com.ucucraft.countries.path.PathRegistry;
import com.ucucraft.countries.vault.VaultManager;

public final class EraRegistry {

    private final Plugin plugin;
    private final VaultManager vaults;
    private final PathRegistry paths;
    private final List<Era> eras = new ArrayList<>();
    private final Map<Material, Integer> gated = new HashMap<>();
    private FileConfiguration config;

    public EraRegistry(Plugin plugin, VaultManager vaults, PathRegistry paths) {
        this.plugin = plugin;
        this.vaults = vaults;
        this.paths = paths;
    }

    public void load() {
        eras.clear();
        gated.clear();

        File file = new File(plugin.getDataFolder(), "eras.yml");
        if (!file.exists()) {
            plugin.saveResource("eras.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);

        RequirementFactory factory = new RequirementFactory(vaults, paths, plugin.getLogger());
        List<Map<?, ?>> raw = config.getMapList("eras");
        int index = 0;
        for (Map<?, ?> entry : raw) {
            ConfigurationSection section = Sections.of(entry);
            String id = section.getString("id");
            if (id == null) {
                plugin.getLogger().warning("Era at position " + index + " has no id, skipping");
                continue;
            }
            List<Requirement> requirements = new ArrayList<>();
            for (Map<?, ?> reqEntry : section.getMapList("requirements")) {
                Requirement requirement = factory.create(Sections.of(reqEntry));
                if (requirement != null) {
                    requirements.add(requirement);
                }
            }
            int baseLimit = section.getInt("base-chunk-limit", -1);
            int chunksPerPlayer = section.getInt("additional-chunks-per-player", 0);
            eras.add(new Era(id, index, section.getString("display", id), requirements, baseLimit, chunksPerPlayer));
            for (String name : section.getStringList("unlocks")) {
                Material material = Material.matchMaterial(name);
                if (material == null) {
                    plugin.getLogger().warning("Unknown material '" + name + "' in eras.yml unlocks");
                    continue;
                }
                gated.merge(material, index, Math::min);
            }
            index++;
        }
        if (eras.isEmpty()) {
            plugin.getLogger().warning("No eras defined in eras.yml; era gating is disabled");
        }
    }

    public List<Era> all() {
        return eras;
    }

    public boolean isEmpty() {
        return eras.isEmpty();
    }

    public int size() {
        return eras.size();
    }

    public Era byIndex(int index) {
        if (eras.isEmpty()) {
            return null;
        }
        return eras.get(Math.max(0, Math.min(index, eras.size() - 1)));
    }

    public Era byId(String id) {
        for (Era era : eras) {
            if (era.getId().equalsIgnoreCase(id)) {
                return era;
            }
        }
        return null;
    }

    /** Minimum era index needed to craft/place/use the material; 0 when never gated. */
    public int requiredIndex(Material material) {
        return gated.getOrDefault(material, 0);
    }

    public FileConfiguration config() {
        return config;
    }
}
