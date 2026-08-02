package com.ucucraft.countries.command.sub;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.model.Country;

/** Shared helpers for the land subcommands (claim / unclaim). */
final class Claims {

    private Claims() {
    }

    /** Country of the player when they are allowed to manage land, otherwise null. */
    static Country manager(Services services, Player player) {
        UUID uuid = player.getUniqueId();
        Country country = services.countries.getByPlayer(uuid);
        if (country == null) {
            services.messages.send(player, "not-in-country");
            return null;
        }
        boolean allowed = services.config.claimLeaderOnly()
                ? country.isLeader(uuid)
                : country.canWithdraw(uuid);
        if (!allowed) {
            services.messages.send(player, services.config.claimLeaderOnly() ? "not-leader" : "claim-not-trusted");
            return null;
        }
        return country;
    }

    static String limitText(Services services, int limit) {
        return limit == Integer.MAX_VALUE ? services.messages.text("claim-unlimited") : String.valueOf(limit);
    }

    /** Name of the country owning the chunk, or the wilderness label. */
    static String ownerName(Services services, UUID owner) {
        if (owner == null) {
            return services.messages.text("claim-wilderness");
        }
        Country country = services.countries.getById(owner);
        return country != null ? country.getName() : services.messages.text("claim-wilderness");
    }
}
