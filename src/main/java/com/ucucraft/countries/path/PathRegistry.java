package com.ucucraft.countries.path;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import com.ucucraft.countries.config.Sections;
import com.ucucraft.countries.era.Era;
import com.ucucraft.countries.era.EraRegistry;
import com.ucucraft.countries.model.Country;
import com.ucucraft.countries.path.buff.BuffFactory;
import com.ucucraft.countries.path.buff.ClaimBuff;
import com.ucucraft.countries.path.buff.EraCostBuff;
import com.ucucraft.countries.path.buff.PathBuff;

/**
 * paths.yml, plus the country-wide buffs that other systems ask for directly.
 * Holds no runtime state, so it can be handed to services built before the path manager.
 */
public final class PathRegistry {

    private final Plugin plugin;
    private final List<Path> paths = new ArrayList<>();
    private FileConfiguration config = new YamlConfiguration();

    public PathRegistry(Plugin plugin) {
        this.plugin = plugin;
    }

    /** Reads paths.yml; tiers are keyed by era id, so eras have to be loaded first. */
    public void load(EraRegistry eras) {
        paths.clear();

        File file = new File(plugin.getDataFolder(), "paths.yml");
        if (!file.exists()) {
            plugin.saveResource("paths.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);

        BuffFactory factory = new BuffFactory(plugin.getLogger());
        ConfigurationSection root = config.getConfigurationSection("paths");
        if (root == null) {
            return;
        }
        for (String id : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            paths.add(new Path(id.toLowerCase(), section.getString("display", id),
                    section.getStringList("description"), tiers(section, eras, factory, id)));
        }
    }

    private List<PathTier> tiers(ConfigurationSection section, EraRegistry eras,
                                 BuffFactory factory, String pathId) {
        List<PathTier> tiers = new ArrayList<>();
        ConfigurationSection root = section.getConfigurationSection("tiers");
        if (root == null) {
            return tiers;
        }
        for (String eraId : root.getKeys(false)) {
            ConfigurationSection tier = root.getConfigurationSection(eraId);
            if (tier == null) {
                continue;
            }
            Era era = eras.byId(eraId);
            if (era == null) {
                plugin.getLogger().warning("Path '" + pathId + "' has a tier for unknown era '" + eraId + "'");
                continue;
            }
            List<PathBuff> buffs = new ArrayList<>();
            for (Map<?, ?> raw : tier.getMapList("effects")) {
                PathBuff buff = factory.create(Sections.of(raw));
                if (buff != null) {
                    buffs.add(buff);
                }
            }
            tiers.add(new PathTier(eraId, era.getIndex(), tier.getString("display", eraId),
                    tier.getStringList("description"), buffs));
        }
        tiers.sort(Comparator.comparingInt(PathTier::getEraIndex));
        return tiers;
    }

    public List<Path> all() {
        return paths;
    }

    public boolean isEmpty() {
        return paths.isEmpty();
    }

    public Path byId(String id) {
        if (id == null) {
            return null;
        }
        for (Path path : paths) {
            if (path.getId().equalsIgnoreCase(id)) {
                return path;
            }
        }
        return null;
    }

    public Path of(Country country) {
        return byId(country.getPathId());
    }

    /** Tiers the country has already reached. */
    public List<PathTier> unlocked(Country country) {
        List<PathTier> tiers = new ArrayList<>();
        Path path = of(country);
        if (path == null) {
            return tiers;
        }
        for (PathTier tier : path.getTiers()) {
            if (tier.getEraIndex() <= country.getEraIndex()) {
                tiers.add(tier);
            }
        }
        return tiers;
    }

    /** First tier the country has not reached yet, or null when the path is complete. */
    public PathTier next(Country country) {
        Path path = of(country);
        if (path == null) {
            return null;
        }
        for (PathTier tier : path.getTiers()) {
            if (tier.getEraIndex() > country.getEraIndex()) {
                return tier;
            }
        }
        return null;
    }

    /** Tiers unlocked by the era the country just entered. */
    public List<PathTier> tiersAt(Country country, int eraIndex) {
        List<PathTier> tiers = new ArrayList<>();
        Path path = of(country);
        if (path == null) {
            return tiers;
        }
        for (PathTier tier : path.getTiers()) {
            if (tier.getEraIndex() == eraIndex) {
                tiers.add(tier);
            }
        }
        return tiers;
    }

    public List<PathBuff> buffs(Country country) {
        List<PathBuff> buffs = new ArrayList<>();
        for (PathTier tier : unlocked(country)) {
            buffs.addAll(tier.getBuffs());
        }
        return buffs;
    }

    public <T extends PathBuff> List<T> buffs(Country country, Class<T> type) {
        List<T> result = new ArrayList<>();
        for (PathBuff buff : buffs(country)) {
            if (type.isInstance(buff)) {
                result.add(type.cast(buff));
            }
        }
        return result;
    }

    /** Extra chunks the path grants; claim limits are country-wide, not per player. */
    public int claimBonus(Country country) {
        if (!enabled()) {
            return 0;
        }
        int bonus = 0;
        for (ClaimBuff buff : buffs(country, ClaimBuff.class)) {
            bonus += buff.bonus(country);
        }
        return bonus;
    }

    /** Factor applied to era resource requirements; 1.0 when the path grants no discount. */
    public double eraCostMultiplier(Country country) {
        if (!enabled()) {
            return 1.0;
        }
        double multiplier = 1.0;
        for (EraCostBuff buff : buffs(country, EraCostBuff.class)) {
            multiplier *= buff.multiplier();
        }
        return multiplier;
    }

    public boolean enabled() {
        return config.getBoolean("settings.enabled", true);
    }

    public boolean leaderOnly() {
        return config.getBoolean("settings.leader-only", true);
    }

    public boolean allowChange() {
        return config.getBoolean("settings.allow-change", false);
    }

    public boolean announceChoice() {
        return config.getBoolean("settings.announce-choice", true);
    }

    public boolean announceUnlock() {
        return config.getBoolean("settings.announce-unlock", true);
    }

    public long effectIntervalTicks() {
        return Math.max(1L, config.getLong("settings.effect-interval-ticks", 40));
    }

    public boolean potionAmbient() {
        return config.getBoolean("settings.potion.ambient", true);
    }

    public boolean potionParticles() {
        return config.getBoolean("settings.potion.particles", false);
    }

    public boolean potionIcon() {
        return config.getBoolean("settings.potion.icon", true);
    }

    public int undergroundY() {
        return config.getInt("settings.underground-y", 40);
    }
}
