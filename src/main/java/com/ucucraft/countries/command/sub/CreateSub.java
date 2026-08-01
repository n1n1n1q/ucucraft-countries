package com.ucucraft.countries.command.sub;

import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.command.SubCommand;
import com.ucucraft.countries.manager.CountryManager.NameStatus;

public final class CreateSub implements SubCommand {

    private final Services services;

    public CreateSub(Services services) {
        this.services = services;
    }

    @Override
    public String name() {
        return "create";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            services.messages.send(player, "usage-create");
            return;
        }
        if (services.countries.hasCountry(player.getUniqueId())) {
            services.messages.send(player, "already-in-country");
            return;
        }
        String name = args[0];
        NameStatus status = services.countries.checkName(name);
        if (!Names.report(services, player, status, name)) {
            return;
        }
        services.countries.create(name, player.getUniqueId(), services.eras.startingIndex());
        if (services.config.announce("created")) {
            services.messages.broadcast("announce-created", "country", name, "player", player.getName());
        } else {
            services.messages.send(player, "created", "country", name);
        }
    }
}
