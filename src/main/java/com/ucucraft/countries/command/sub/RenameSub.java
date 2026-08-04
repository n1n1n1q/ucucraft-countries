package com.ucucraft.countries.command.sub;

import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.command.SubCommand;
import com.ucucraft.countries.manager.CountryManager.NameStatus;
import com.ucucraft.countries.model.Country;

public final class RenameSub implements SubCommand {

    private final Services services;

    public RenameSub(Services services) {
        this.services = services;
    }

    @Override
    public String name() {
        return "rename";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            services.messages.send(player, "usage-rename");
            return;
        }
        Country country = services.countries.getByPlayer(player.getUniqueId());
        if (country == null) {
            services.messages.send(player, "not-in-country");
            return;
        }
        if (!country.isLeader(player.getUniqueId())) {
            services.messages.send(player, "not-leader");
            return;
        }
        String name = Names.join(args, 0);
        NameStatus status = services.countries.checkName(name);
        if (!Names.report(services, player, status, name)) {
            return;
        }
        String oldName = country.getName();
        services.countries.rename(country, name);
        if (services.config.announce("renamed")) {
            services.messages.broadcast("announce-renamed", "old", oldName, "country", name);
        } else {
            services.messages.send(player, "renamed", "country", name);
        }
    }
}
