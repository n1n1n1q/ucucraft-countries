package com.ucucraft.countries.command;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.command.sub.PathSub;

/** Shortcut for /country path, so choosing a path is a single command. */
public final class PathCommand implements TabExecutor {

    private final Services services;
    private final PathSub sub;

    public PathCommand(Services services) {
        this.services = services;
        this.sub = new PathSub(services);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            services.messages.send(sender, "player-only");
            return true;
        }
        sub.execute(player, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        return sender instanceof Player player ? sub.complete(player, args) : List.of();
    }
}
