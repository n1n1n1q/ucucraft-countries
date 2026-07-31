package com.ucucraft.countries.config;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class Messages {

    private final Plugin plugin;
    private final MiniMessage mini = MiniMessage.miniMessage();
    private YamlConfiguration lang;
    private YamlConfiguration defaults;
    private String prefix;

    public Messages(Plugin plugin) {
        this.plugin = plugin;
    }

    public void load(String language) {
        File folder = new File(plugin.getDataFolder(), "lang");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String resource = "lang/" + language + ".yml";
        File file = new File(plugin.getDataFolder(), resource);
        if (!file.exists()) {
            if (plugin.getResource(resource) != null) {
                plugin.saveResource(resource, false);
            } else {
                plugin.saveResource("lang/en.yml", false);
                file = new File(plugin.getDataFolder(), "lang/en.yml");
            }
        }
        this.lang = YamlConfiguration.loadConfiguration(file);
        this.defaults = loadBundled("lang/en.yml");
        this.prefix = raw("prefix");
    }

    private YamlConfiguration loadBundled(String resource) {
        InputStream in = plugin.getResource(resource);
        if (in == null) {
            return new YamlConfiguration();
        }
        return YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    private String raw(String key) {
        String value = lang.getString(key);
        if (value == null && defaults != null) {
            value = defaults.getString(key);
        }
        return value != null ? value : key;
    }

    private String apply(String text, String... placeholders) {
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            text = text.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }
        return text;
    }

    public Component component(String key, String... placeholders) {
        return mini.deserialize(apply(raw(key), placeholders));
    }

    public void send(CommandSender sender, String key, String... placeholders) {
        sender.sendMessage(mini.deserialize(prefix + apply(raw(key), placeholders)));
    }

    public void sendRaw(CommandSender sender, String key, String... placeholders) {
        sender.sendMessage(component(key, placeholders));
    }
}
