package com.ucucraft.countries.command.sub;

import java.util.List;

import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.command.NameArgs;
import com.ucucraft.countries.command.SubCommand;
import com.ucucraft.countries.manager.DiplomacyManager.Kind;
import com.ucucraft.countries.manager.DiplomacyManager.Offer;
import com.ucucraft.countries.model.Country;

public final class AllianceSub implements SubCommand {

    private final Services services;

    public AllianceSub(Services services) {
        this.services = services;
    }

    @Override
    public String name() {
        return "ally";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            services.messages.send(player, "usage-ally");
            return;
        }
        switch (args[0].toLowerCase()) {
            case "list" -> list(player);
            case "invite" -> invite(player, args);
            case "accept" -> accept(player, args);
            case "disband" -> disband(player, args);
            default -> services.messages.send(player, "usage-ally");
        }
    }

    private void list(Player player) {
        Country own = Diplo.own(services, player, false);
        if (own == null) {
            return;
        }
        services.messages.send(player, "ally-list", "allies", Diplo.names(services, own.getAllies()));
    }

    private void invite(Player player, String[] args) {
        if (args.length < 2) {
            services.messages.send(player, "usage-ally");
            return;
        }
        Country own = Diplo.own(services, player, true);
        if (own == null) {
            return;
        }
        Country target = Diplo.target(services, player, Names.join(args, 1));
        if (target == null) {
            return;
        }
        if (target.getId().equals(own.getId())) {
            services.messages.send(player, "diplo-self");
            return;
        }
        if (own.isAllied(target.getId())) {
            services.messages.send(player, "ally-already", "country", target.getName());
            return;
        }
        if (own.isAtWar(target.getId())) {
            services.messages.send(player, "ally-at-war", "country", target.getName());
            return;
        }
        if (services.diplomacy.has(target.getId(), Kind.ALLY, own.getId())) {
            services.messages.send(player, "ally-invite-already", "country", target.getName());
            return;
        }
        services.diplomacy.offer(target.getId(), Kind.ALLY, own.getId(), own.getName());
        services.messages.send(player, "ally-invite-sent", "country", target.getName(),
                "seconds", String.valueOf(services.config.diplomacyDurationSeconds()));
        Diplo.notifyLeader(services, target, "ally-invite-received", "country", own.getName());
    }

    private void accept(Player player, String[] args) {
        if (args.length < 2) {
            services.messages.send(player, "usage-ally");
            return;
        }
        Country own = Diplo.own(services, player, true);
        if (own == null) {
            return;
        }
        Country target = Diplo.target(services, player, Names.join(args, 1));
        if (target == null) {
            return;
        }
        Offer offer = services.diplomacy.take(own.getId(), Kind.ALLY, target.getId());
        if (offer == null || offer.isExpired()) {
            services.messages.send(player, "ally-invite-none", "country", target.getName());
            return;
        }
        services.countries.ally(own, target);
        if (services.config.announce("alliance")) {
            services.messages.broadcast("announce-alliance", "a", target.getName(), "b", own.getName());
        } else {
            services.messages.send(player, "ally-formed", "country", target.getName());
        }
    }

    private void disband(Player player, String[] args) {
        if (args.length < 2) {
            services.messages.send(player, "usage-ally");
            return;
        }
        Country own = Diplo.own(services, player, true);
        if (own == null) {
            return;
        }
        Country target = Diplo.target(services, player, Names.join(args, 1));
        if (target == null) {
            return;
        }
        if (!own.isAllied(target.getId())) {
            services.messages.send(player, "ally-not-allied", "country", target.getName());
            return;
        }
        services.countries.unally(own, target);
        if (services.config.announce("alliance")) {
            services.messages.broadcast("announce-alliance-ended", "a", own.getName(), "b", target.getName());
        } else {
            services.messages.send(player, "ally-ended", "country", target.getName());
        }
    }

    @Override
    public List<String> complete(Player player, String[] args) {
        if (args.length == 1) {
            return Diplo.filter(List.of("invite", "accept", "disband", "list"), args[0]);
        }
        Country own = services.countries.getByPlayer(player.getUniqueId());
        if (own == null) {
            return List.of();
        }
        List<String> names = switch (args[0].toLowerCase()) {
            case "invite", "accept" -> Diplo.otherCountryNames(services, own);
            case "disband" -> Diplo.countryNames(services, own.getAllies());
            default -> List.of();
        };
        return NameArgs.completeName(names, args, 1);
    }
}
