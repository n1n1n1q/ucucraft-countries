package com.ucucraft.countries.command.sub;

import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.command.SubCommand;
import com.ucucraft.countries.model.Country;

public final class LeaveSub implements SubCommand {

    private final Services services;

    public LeaveSub(Services services) {
        this.services = services;
    }

    @Override
    public String name() {
        return "leave";
    }

    @Override
    public void execute(Player player, String[] args) {
        Country country = services.countries.getByPlayer(player.getUniqueId());
        if (country == null) {
            services.messages.send(player, "not-in-country");
            return;
        }
        if (country.isLeader(player.getUniqueId())) {
            services.messages.send(player, "leader-cant-leave");
            return;
        }
        String name = country.getName();
        services.countries.removeMember(country, player.getUniqueId());
        services.messages.send(player, "left", "country", name);
    }
}
