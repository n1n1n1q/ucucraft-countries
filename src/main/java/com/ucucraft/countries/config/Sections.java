package com.ucucraft.countries.config;

import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class Sections {

    private Sections() {
    }

    /** Nested maps have to become real sections; set() would store them as plain maps. */
    public static ConfigurationSection of(Map<?, ?> map) {
        YamlConfiguration section = new YamlConfiguration();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = String.valueOf(entry.getKey());
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> child) {
                section.createSection(key, child);
            } else {
                section.set(key, value);
            }
        }
        return section;
    }
}
