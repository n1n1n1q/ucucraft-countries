package com.ucucraft.countries.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.command.sub.AcceptSub;

public final class AcceptCommand implements TabExecutor {

    private final Services services;

    public AcceptCommand(Services services) {
        this.services = services;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            services.messages.send(sender, "player-only");
            return true;
        }
        AcceptSub.handle(services, player, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player && args.length == 1) {
            return services.invites.pendingInviterNames(player.getUniqueId());
        }
        return List.of();
    }
}
