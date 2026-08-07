package com.ucucraft.countries.path.buff;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffectType;

import com.ucucraft.countries.config.Sections;

/** Turns a paths.yml effect entry into a {@link PathBuff}. */
public final class BuffFactory {

    private final Logger logger;

    public BuffFactory(Logger logger) {
        this.logger = logger;
    }

    public PathBuff create(ConfigurationSection section) {
        String type = section.getString("type", "").toLowerCase();
        BuffScope scope = BuffScope.parse(section, logger);
        return switch (type) {
            case "attribute" -> attribute(section, scope);
            case "potion" -> potion(section, scope);
            case "damage" -> damage(section, scope);
            case "drops" -> drops(section, scope);
            case "claim-limit" -> new ClaimBuff(section.getInt("flat", 0), section.getInt("per-member", 0));
            case "era-cost" -> new EraCostBuff(section.getDouble("multiplier", 1.0));
            case "ability" -> ability(section, scope);
            default -> {
                logger.warning("Unknown buff type '" + type + "' in paths.yml");
                yield null;
            }
        };
    }

    private PathBuff attribute(ConfigurationSection section, BuffScope scope) {
        String name = section.getString("attribute", "");
        NamespacedKey key = key(name);
        Attribute attribute = key != null ? Registry.ATTRIBUTE.get(key) : null;
        if (attribute == null) {
            logger.warning("Unknown attribute '" + name + "' in paths.yml");
            return null;
        }
        Operation operation;
        try {
            operation = Operation.valueOf(section.getString("operation", "add_number").toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warning("Unknown attribute operation '" + section.getString("operation") + "' in paths.yml");
            return null;
        }
        return new AttributeBuff(attribute, section.getDouble("amount", 0), operation, scope);
    }

    private PathBuff potion(ConfigurationSection section, BuffScope scope) {
        PotionEffectType type = potionType(section.getString("potion", ""));
        return type == null ? null : new PotionBuff(type, section.getInt("amplifier", 0), scope);
    }

    private PathBuff damage(ConfigurationSection section, BuffScope scope) {
        DamageBuff.Direction direction = "dealt".equalsIgnoreCase(section.getString("direction", "taken"))
                ? DamageBuff.Direction.DEALT : DamageBuff.Direction.TAKEN;
        Set<DamageCause> causes = EnumSet.noneOf(DamageCause.class);
        for (String name : section.getStringList("causes")) {
            try {
                causes.add(DamageCause.valueOf(name.trim().toUpperCase()));
            } catch (IllegalArgumentException e) {
                logger.warning("Unknown damage cause '" + name + "' in paths.yml");
            }
        }
        return new DamageBuff(direction, section.getDouble("multiplier", 1.0), causes, scope);
    }

    private PathBuff drops(ConfigurationSection section, BuffScope scope) {
        Set<Material> materials = new LinkedHashSet<>();
        for (String name : section.getStringList("materials")) {
            Material material = Material.matchMaterial(name);
            if (material == null) {
                logger.warning("Unknown material '" + name + "' in paths.yml");
                continue;
            }
            materials.add(material);
        }
        return new DropBuff(section.getDouble("multiplier", 1.0), materials, scope);
    }

    private PathBuff ability(ConfigurationSection section, BuffScope scope) {
        String id = section.getString("id");
        if (id == null) {
            logger.warning("Ability without an 'id' in paths.yml");
            return null;
        }
        AbilityBuff.Target target;
        try {
            target = AbilityBuff.Target.valueOf(section.getString("target", "self").toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warning("Unknown ability target '" + section.getString("target") + "' in paths.yml");
            return null;
        }
        List<AbilityBuff.TimedPotion> effects = new ArrayList<>();
        for (Map<?, ?> raw : section.getMapList("effects")) {
            ConfigurationSection child = Sections.of(raw);
            PotionEffectType type = potionType(child.getString("potion", ""));
            if (type != null) {
                effects.add(new AbilityBuff.TimedPotion(type,
                        child.getInt("amplifier", 0), child.getInt("seconds", 30)));
            }
        }
        return new AbilityBuff(id.toLowerCase(), section.getString("display", id), target,
                section.getDouble("radius", 16), section.getLong("cooldown-seconds", 0),
                section.getBoolean("broadcast", false), effects, scope);
    }

    private PotionEffectType potionType(String name) {
        NamespacedKey key = key(name);
        PotionEffectType type = key != null ? Registry.POTION_EFFECT_TYPE.get(key) : null;
        if (type == null) {
            logger.warning("Unknown potion effect '" + name + "' in paths.yml");
        }
        return type;
    }

    /** Null for names that are not a valid key at all, so the caller can report them. */
    private NamespacedKey key(String name) {
        try {
            return NamespacedKey.minecraft(name.trim().toLowerCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
