package com.ucucraft.countries.command.sub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.command.SubCommand;
import com.ucucraft.countries.model.Country;

public final class InviteSub implements SubCommand {

    private final Services services;

    public InviteSub(Services services) {
        this.services = services;
    }

    @Override
    public String name() {
        return "invite";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            services.messages.send(player, "usage-invite");
            return;
        }
        Country country = services.countries.getByPlayer(player.getUniqueId());
        if (country == null) {
            services.messages.send(player, "not-in-country");
            return;
        }
        if (services.config.inviteLeaderOnly() && !country.isLeader(player.getUniqueId())) {
            services.messages.send(player, "not-leader");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            services.messages.send(player, "target-offline");
            return;
        }
        if (target.equals(player)) {
            services.messages.send(player, "target-self");
            return;
        }
        if (services.countries.hasCountry(target.getUniqueId())) {
            services.messages.send(player, "target-has-country");
            return;
        }
        if (services.invites.hasInvite(target.getUniqueId(), player.getName())) {
            services.messages.send(player, "invite-already-sent");
            return;
        }
        services.invites.invite(target.getUniqueId(), player.getUniqueId(), player.getName(),
                country.key(), country.getName());
        services.messages.send(player, "invite-sent", "player", target.getName(), "country", country.getName());
        services.messages.send(target, "invite-received",
                "player", player.getName(),
                "country", country.getName(),
                "seconds", String.valueOf(services.config.inviteDurationSeconds()));
    }

    @Override
    public List<String> complete(Player player, String[] args) {
        if (args.length == 1) {
            List<String> names = new ArrayList<>();
            for (Player online : Bukkit.getOnlinePlayers()) {
                if (!online.equals(player) && !services.countries.hasCountry(online.getUniqueId())) {
                    names.add(online.getName());
                }
            }
            return names;
        }
        return List.of();
    }
}
