package com.ucucraft.countries.command.sub;

import java.util.List;

import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.command.SubCommand;
import com.ucucraft.countries.model.Country;

public final class InfoSub implements SubCommand {

    private final Services services;

    public InfoSub(Services services) {
        this.services = services;
    }

    @Override
    public String name() {
        return "info";
    }

    @Override
    public void execute(Player player, String[] args) {
        Country country;
        if (args.length >= 1) {
            country = services.countries.getByName(args[0]);
            if (country == null) {
                services.messages.send(player, "country-not-found", "country", args[0]);
                return;
            }
        } else {
            country = services.countries.getByPlayer(player.getUniqueId());
            if (country == null) {
                services.messages.send(player, "not-in-country");
                return;
            }
        }
        services.messages.sendRaw(player, "info-header", "country", country.getName());
        services.messages.sendRaw(player, "info-leader", "leader", country.getLeaderName());
        services.messages.sendRaw(player, "info-members",
                "members", String.join(", ", country.getMemberNames()));
    }

    @Override
    public List<String> complete(Player player, String[] args) {
        if (args.length == 1) {
            return services.countries.allSorted().stream().map(Country::getName).toList();
        }
        return List.of();
    }
}
