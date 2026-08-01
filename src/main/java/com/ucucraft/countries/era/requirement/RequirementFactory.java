package com.ucucraft.countries.era.requirement;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;

import com.ucucraft.countries.vault.VaultManager;

public final class RequirementFactory {

    private final VaultManager vaults;
    private final Logger logger;

    public RequirementFactory(VaultManager vaults, Logger logger) {
        this.vaults = vaults;
        this.logger = logger;
    }

    public Requirement create(ConfigurationSection section) {
        String type = section.getString("type", "").toLowerCase();
        switch (type) {
            case "resource":
                return resource(section);
            case "admin":
                return admin(section);
            case "statistic":
                return statistic(section);
            case "time_in_era":
                return new TimeInEraRequirement(section.getLong("hours", 1));
            case "members":
                return new MembersRequirement(section.getInt("amount", 1));
            default:
                logger.warning("Unknown requirement type '" + type + "' in eras.yml");
                return null;
        }
    }

    private Requirement resource(ConfigurationSection section) {
        ConfigurationSection items = section.getConfigurationSection("items");
        if (items == null) {
            logger.warning("Resource requirement without an 'items' section in eras.yml");
            return null;
        }
        Map<Material, Integer> map = new LinkedHashMap<>();
        for (String key : items.getKeys(false)) {
            Material material = Material.matchMaterial(key);
            if (material == null) {
                logger.warning("Unknown material '" + key + "' in eras.yml");
                continue;
            }
            map.put(material, items.getInt(key));
        }
        return map.isEmpty() ? null : new ResourceRequirement(vaults, map);
    }

    private Requirement admin(ConfigurationSection section) {
        String id = section.getString("id");
        if (id == null) {
            logger.warning("Admin requirement without an 'id' in eras.yml");
            return null;
        }
        return new AdminRequirement(id, section.getString("display", id));
    }

    private Requirement statistic(ConfigurationSection section) {
        String name = section.getString("statistic", "");
        Statistic statistic;
        try {
            statistic = Statistic.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warning("Unknown statistic '" + name + "' in eras.yml");
            return null;
        }
        Material material = section.contains("material")
                ? Material.matchMaterial(section.getString("material", "")) : null;
        EntityType entity = null;
        if (section.contains("entity")) {
            try {
                entity = EntityType.valueOf(section.getString("entity", "").toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.warning("Unknown entity '" + section.getString("entity") + "' in eras.yml");
            }
        }
        boolean total = !"any".equalsIgnoreCase(section.getString("scope", "total"));
        return new StatisticRequirement(statistic, material, entity, section.getInt("amount", 1), total);
    }
}
