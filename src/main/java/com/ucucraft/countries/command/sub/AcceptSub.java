package com.ucucraft.countries.command.sub;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.command.SubCommand;
import com.ucucraft.countries.manager.InviteManager.Invite;
import com.ucucraft.countries.model.Country;

public final class AcceptSub implements SubCommand {

    private final Services services;

    public AcceptSub(Services services) {
        this.services = services;
    }

    @Override
    public String name() {
        return "accept";
    }

    @Override
    public void execute(Player player, String[] args) {
        handle(services, player, args);
    }

    @Override
    public List<String> complete(Player player, String[] args) {
        if (args.length == 1) {
            return services.invites.pendingInviterNames(player.getUniqueId());
        }
        return List.of();
    }

    public static void handle(Services services, Player player, String[] args) {
        if (args.length < 1) {
            services.messages.send(player, "usage-accept");
            return;
        }
        if (services.countries.hasCountry(player.getUniqueId())) {
            services.messages.send(player, "already-in-country");
            return;
        }
        Invite invite = services.invites.take(player.getUniqueId(), args[0]);
        if (invite == null) {
            services.messages.send(player, "invite-none", "player", args[0]);
            return;
        }
        if (invite.isExpired()) {
            services.messages.send(player, "invite-expired");
            return;
        }
        Country country = services.countries.getByName(invite.getCountryName());
        if (country == null) {
            services.messages.send(player, "invite-expired");
            return;
        }
        services.countries.addMember(country, player.getUniqueId());
        services.messages.send(player, "joined", "country", country.getName());
        for (java.util.UUID member : country.getMembers()) {
            if (member.equals(player.getUniqueId())) {
                continue;
            }
            OfflinePlayer off = Bukkit.getOfflinePlayer(member);
            if (off.isOnline()) {
                services.messages.send(off.getPlayer(), "member-joined", "player", player.getName());
            }
        }
    }
}
