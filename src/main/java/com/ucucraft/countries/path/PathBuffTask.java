package com.ucucraft.countries.path;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.ucucraft.countries.manager.CountryManager;
import com.ucucraft.countries.model.Country;
import com.ucucraft.countries.path.buff.AttributeBuff;
import com.ucucraft.countries.path.buff.PotionBuff;

/** Keeps the passive buffs (attributes and potions) in sync with where every player is. */
public final class PathBuffTask implements Runnable {

    private final Plugin plugin;
    private final PathManager paths;
    private final CountryManager countries;
    private final Map<UUID, Set<Attribute>> attributes = new HashMap<>();
    private final Map<UUID, Set<PotionEffectType>> potions = new HashMap<>();

    public PathBuffTask(Plugin plugin, PathManager paths, CountryManager countries) {
        this.plugin = plugin;
        this.paths = paths;
        this.countries = countries;
    }

    @Override
    public void run() {
        attributes.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
        potions.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
        for (Player player : Bukkit.getOnlinePlayers()) {
            Country country = countries.getByPlayer(player.getUniqueId());
            if (country != null && !country.hasPath()) {
                country = null;
            }
            applyAttributes(player, country);
            applyPotions(player, country);
        }
    }

    private void applyAttributes(Player player, Country country) {
        Map<Attribute, Map<Operation, Double>> wanted = new LinkedHashMap<>();
        if (country != null) {
            for (AttributeBuff buff : paths.active(player, country, AttributeBuff.class)) {
                wanted.computeIfAbsent(buff.attribute(), key -> new EnumMap<>(Operation.class))
                        .merge(buff.operation(), buff.amount(), Double::sum);
            }
        }
        UUID uuid = player.getUniqueId();
        Set<Attribute> touched = new HashSet<>(attributes.getOrDefault(uuid, Set.of()));
        touched.addAll(wanted.keySet());
        for (Attribute attribute : touched) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance == null) {
                continue;
            }
            Map<Operation, Double> amounts = wanted.getOrDefault(attribute, Map.of());
            for (Operation operation : Operation.values()) {
                sync(instance, key(attribute, operation), operation, amounts.get(operation));
            }
        }
        if (wanted.isEmpty()) {
            attributes.remove(uuid);
        } else {
            attributes.put(uuid, new HashSet<>(wanted.keySet()));
        }
    }

    private void sync(AttributeInstance instance, NamespacedKey key, Operation operation, Double amount) {
        AttributeModifier existing = instance.getModifier(key);
        if (amount == null || amount == 0) {
            if (existing != null) {
                instance.removeModifier(existing);
            }
            return;
        }
        if (existing != null) {
            if (existing.getAmount() == amount) {
                return;
            }
            instance.removeModifier(existing);
        }
        instance.addTransientModifier(new AttributeModifier(key, amount, operation));
    }

    private NamespacedKey key(Attribute attribute, Operation operation) {
        return new NamespacedKey(plugin,
                "path." + attribute.getKey().getKey() + "." + operation.name().toLowerCase());
    }

    private void applyPotions(Player player, Country country) {
        Map<PotionEffectType, Integer> wanted = new LinkedHashMap<>();
        if (country != null) {
            for (PotionBuff buff : paths.active(player, country, PotionBuff.class)) {
                wanted.merge(buff.type(), buff.amplifier(), Math::max);
            }
        }
        int duration = (int) paths.registry().effectIntervalTicks() * 2 + 20;
        UUID uuid = player.getUniqueId();
        for (PotionEffectType type : potions.getOrDefault(uuid, Set.of())) {
            PotionEffect current = player.getPotionEffect(type);
            if (!wanted.containsKey(type) && current != null && current.getDuration() <= duration) {
                player.removePotionEffect(type);
            }
        }
        for (Map.Entry<PotionEffectType, Integer> entry : wanted.entrySet()) {
            PotionEffect current = player.getPotionEffect(entry.getKey());
            if (current != null && (current.getAmplifier() > entry.getValue()
                    || current.getDuration() > duration)) {
                continue;
            }
            player.addPotionEffect(new PotionEffect(entry.getKey(), duration, entry.getValue(),
                    paths.registry().potionAmbient(), paths.registry().potionParticles(),
                    paths.registry().potionIcon()));
        }
        if (wanted.isEmpty()) {
            potions.remove(uuid);
        } else {
            potions.put(uuid, new HashSet<>(wanted.keySet()));
        }
    }
}
