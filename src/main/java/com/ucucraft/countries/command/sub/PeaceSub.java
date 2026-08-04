package com.ucucraft.countries.command.sub;

import java.util.List;

import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.command.SubCommand;
import com.ucucraft.countries.manager.DiplomacyManager.Kind;
import com.ucucraft.countries.model.Country;

public final class PeaceSub implements SubCommand {

    private final Services services;

    public PeaceSub(Services services) {
        this.services = services;
    }

    @Override
    public String name() {
        return "peace";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            services.messages.send(player, "usage-peace");
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
        if (!own.isAtWar(target.getId())) {
            services.messages.send(player, "peace-not-at-war", "country", target.getName());
            return;
        }
        // The other side already offered peace -> both agree, war ends.
        if (services.diplomacy.has(own.getId(), Kind.PEACE, target.getId())) {
            services.diplomacy.take(own.getId(), Kind.PEACE, target.getId());
            services.diplomacy.take(target.getId(), Kind.PEACE, own.getId());
            services.countries.makePeace(own, target);
            if (services.config.announce("peace")) {
                services.messages.broadcast("announce-peace", "a", own.getName(), "b", target.getName());
            } else {
                services.messages.send(player, "peace-made", "country", target.getName());
            }
            return;
        }
        if (services.diplomacy.has(target.getId(), Kind.PEACE, own.getId())) {
            services.messages.send(player, "peace-already", "country", target.getName());
            return;
        }
        services.diplomacy.offer(target.getId(), Kind.PEACE, own.getId(), own.getName());
        services.messages.send(player, "peace-offered", "country", target.getName(),
                "seconds", String.valueOf(services.config.diplomacyDurationSeconds()));
        Diplo.notifyLeader(services, target, "peace-received", "country", own.getName());
    }

    @Override
    public List<String> complete(Player player, String[] args) {
        if (args.length == 1) {
            Country own = services.countries.getByPlayer(player.getUniqueId());
            if (own != null) {
                return Diplo.countryNames(services, own.getWars());
            }
        }
        return List.of();
    }
}
