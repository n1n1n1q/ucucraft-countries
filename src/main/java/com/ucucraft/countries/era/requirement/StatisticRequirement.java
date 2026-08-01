package com.ucucraft.countries.era.requirement;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.EntityType;

import com.ucucraft.countries.model.Country;

public final class StatisticRequirement implements Requirement {

    private final Statistic statistic;
    private final Material material;
    private final EntityType entity;
    private final int amount;
    private final boolean total;

    public StatisticRequirement(Statistic statistic, Material material, EntityType entity,
                                int amount, boolean total) {
        this.statistic = statistic;
        this.material = material;
        this.entity = entity;
        this.amount = amount;
        this.total = total;
    }

    private int valueOf(OfflinePlayer player) {
        try {
            if (material != null) {
                return player.getStatistic(statistic, material);
            }
            if (entity != null) {
                return player.getStatistic(statistic, entity);
            }
            return player.getStatistic(statistic);
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    private int current(Country country) {
        int best = 0;
        int sum = 0;
        for (UUID uuid : country.getMembers()) {
            int value = valueOf(Bukkit.getOfflinePlayer(uuid));
            sum += value;
            best = Math.max(best, value);
        }
        return total ? sum : best;
    }

    @Override
    public String type() {
        return "statistic";
    }

    @Override
    public boolean isMet(Country country) {
        return current(country) >= amount;
    }

    @Override
    public String progress(Country country) {
        return Math.min(current(country), amount) + "/" + amount;
    }

    @Override
    public String descriptionKey() {
        return "era-req-statistic";
    }

    @Override
    public String[] placeholders(Country country) {
        String target = material != null ? ResourceRequirement.pretty(material)
                : entity != null ? entity.name().toLowerCase().replace('_', ' ')
                : "";
        return new String[]{
                "statistic", statistic.name().toLowerCase().replace('_', ' '),
                "target", target,
                "progress", progress(country)
        };
    }
}
