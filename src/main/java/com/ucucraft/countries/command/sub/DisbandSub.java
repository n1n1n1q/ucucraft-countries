package com.ucucraft.countries.command.sub;

import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.command.SubCommand;
import com.ucucraft.countries.model.Country;

public final class DisbandSub implements SubCommand {

    private final Services services;

    public DisbandSub(Services services) {
        this.services = services;
    }

    @Override
    public String name() {
        return "disband";
    }

    @Override
    public void execute(Player player, String[] args) {
        Country country = services.countries.getByPlayer(player.getUniqueId());
        if (country == null) {
            services.messages.send(player, "not-in-country");
            return;
        }
        if (!country.isLeader(player.getUniqueId())) {
            services.messages.send(player, "not-leader");
            return;
        }
        String name = country.getName();
        services.diplomacy.removeAllInvolving(country.getId());
        services.vaults.remove(country);
        services.vaults.save();
        services.countries.disband(country);
        if (services.config.announce("disbanded")) {
            services.messages.broadcast("announce-disbanded", "country", name, "player", player.getName());
        } else {
            services.messages.send(player, "disbanded", "country", name);
        }
    }
}
