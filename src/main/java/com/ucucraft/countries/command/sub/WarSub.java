package com.ucucraft.countries.command.sub;

import java.util.List;

import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.command.SubCommand;
import com.ucucraft.countries.manager.DiplomacyManager.Kind;
import com.ucucraft.countries.model.Country;

public final class WarSub implements SubCommand {

    private final Services services;

    public WarSub(Services services) {
        this.services = services;
    }

    @Override
    public String name() {
        return "war";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            services.messages.send(player, "usage-war");
            return;
        }
        Country own = Diplo.own(services, player, true);
        if (own == null) {
            return;
        }
        Country target = Diplo.target(services, player, Names.join(args, 0));
        if (target == null) {
            return;
        }
        if (target.getId().equals(own.getId())) {
            services.messages.send(player, "diplo-self");
            return;
        }
        if (own.isAtWar(target.getId())) {
            services.messages.send(player, "war-already", "country", target.getName());
            return;
        }
        if (own.isAllied(target.getId())) {
            services.countries.unally(own, target);
        }
        services.diplomacy.take(own.getId(), Kind.PEACE, target.getId());
        services.diplomacy.take(target.getId(), Kind.PEACE, own.getId());
        services.countries.declareWar(own, target);
        if (services.config.announce("war")) {
            services.messages.broadcast("announce-war", "a", own.getName(), "b", target.getName());
        } else {
            services.messages.send(player, "war-declared", "country", target.getName());
        }
    }

    @Override
    public List<String> complete(Player player, String[] args) {
        if (args.length == 1) {
            Country own = services.countries.getByPlayer(player.getUniqueId());
            if (own != null) {
                return Diplo.otherCountryNames(services, own);
            }
        }
        return List.of();
    }
}
