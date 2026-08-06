package com.ucucraft.countries.path.buff;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;

/** Conditions attached to a buff; all of them must hold for it to apply. */
public final class BuffScope {

    public static final BuffScope ALWAYS = new BuffScope(List.of());

    private final List<Condition> conditions;

    private BuffScope(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public static BuffScope parse(ConfigurationSection section, Logger logger) {
        List<String> names = section.isList("scope")
                ? section.getStringList("scope")
                : (section.isString("scope") ? List.of(section.getString("scope", "")) : List.of());
        List<Condition> conditions = new ArrayList<>();
        for (String name : names) {
            Condition condition = Condition.parse(name);
            if (condition == null) {
                logger.warning("Unknown buff scope '" + name + "' in paths.yml");
            } else if (condition != Condition.ALWAYS) {
                conditions.add(condition);
            }
        }
        return conditions.isEmpty() ? ALWAYS : new BuffScope(conditions);
    }

    public boolean test(BuffContext context) {
        for (Condition condition : conditions) {
            if (!condition.test(context)) {
                return false;
            }
        }
        return true;
    }

    public enum Condition {
        ALWAYS, OWN_CLAIMS, FOREIGN_CLAIMS, ENEMY_CLAIMS, ANY_CLAIMS, WILDERNESS,
        AT_WAR, AT_PEACE, DAY, NIGHT, SNEAKING, UNDERGROUND;

        static Condition parse(String name) {
            try {
                return valueOf(name.trim().toUpperCase().replace('-', '_'));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        boolean test(BuffContext context) {
            return switch (this) {
                case ALWAYS -> true;
                case OWN_CLAIMS -> context.onOwnClaim();
                case FOREIGN_CLAIMS -> context.onForeignClaim();
                case ENEMY_CLAIMS -> context.onEnemyClaim();
                case ANY_CLAIMS -> context.owner() != null;
                case WILDERNESS -> context.owner() == null;
                case AT_WAR -> !context.country().getWars().isEmpty();
                case AT_PEACE -> context.country().getWars().isEmpty();
                case DAY -> !context.isNight();
                case NIGHT -> context.isNight();
                case SNEAKING -> context.player().isSneaking();
                case UNDERGROUND -> context.player().getLocation().getY() < context.undergroundY();
            };
        }
    }
}
