package com.ucucraft.countries.command.sub;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.model.Country;

/** Shared helpers for the diplomacy subcommands (ally / war / peace). */
final class Diplo {

    private Diplo() {
    }

    static Country own(Services services, Player player, boolean requireLeader) {
        Country country = services.countries.getByPlayer(player.getUniqueId());
        if (country == null) {
            services.messages.send(player, "not-in-country");
            return null;
        }
        if (requireLeader && !country.isLeader(player.getUniqueId())) {
            services.messages.send(player, "not-leader");
            return null;
        }
        return country;
    }

    static Country target(Services services, Player player, String name) {
        Country target = services.countries.getByName(name);
        if (target == null) {
            services.messages.send(player, "country-not-found", "country", name);
        }
        return target;
    }

    static void notifyLeader(Services services, Country country, String key, String... placeholders) {
        OfflinePlayer leader = Bukkit.getOfflinePlayer(country.getLeader());
        if (leader.isOnline()) {
            services.messages.send(leader.getPlayer(), key, placeholders);
        }
    }

    static String names(Services services, Set<UUID> ids) {
        List<String> names = countryNames(services, ids);
        return names.isEmpty() ? services.messages.text("none") : String.join(", ", names);
    }

    static List<String> countryNames(Services services, Set<UUID> ids) {
        List<String> names = new ArrayList<>();
        for (UUID id : ids) {
            Country country = services.countries.getById(id);
            if (country != null) {
                names.add(country.getName());
            }
        }
        return names;
    }

    static List<String> otherCountryNames(Services services, Country own) {
        List<String> names = new ArrayList<>();
        for (Country country : services.countries.allSorted()) {
            if (!country.getId().equals(own.getId())) {
                names.add(country.getName());
            }
        }
        return names;
    }

    static List<String> filter(List<String> options, String prefix) {
        List<String> result = new ArrayList<>();
        String lower = prefix.toLowerCase();
        for (String option : options) {
            if (option.startsWith(lower)) {
                result.add(option);
            }
        }
        return result;
    }
}
