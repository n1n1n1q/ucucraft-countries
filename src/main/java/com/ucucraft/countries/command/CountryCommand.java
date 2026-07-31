package com.ucucraft.countries.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.command.sub.AcceptSub;
import com.ucucraft.countries.command.sub.CreateSub;
import com.ucucraft.countries.command.sub.DisbandSub;
import com.ucucraft.countries.command.sub.InfoSub;
import com.ucucraft.countries.command.sub.InviteSub;
import com.ucucraft.countries.command.sub.LeaveSub;
import com.ucucraft.countries.command.sub.ListSub;
import com.ucucraft.countries.command.sub.RenameSub;

public final class CountryCommand implements TabExecutor {

    private final Services services;
    private final Map<String, SubCommand> subs = new LinkedHashMap<>();

    public CountryCommand(Services services) {
        this.services = services;
        register(new CreateSub(services));
        register(new InviteSub(services));
        register(new AcceptSub(services));
        register(new LeaveSub(services));
        register(new DisbandSub(services));
        register(new RenameSub(services));
        register(new InfoSub(services));
        register(new ListSub(services));
    }

    private void register(SubCommand sub) {
        subs.put(sub.name(), sub);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            services.messages.send(sender, "player-only");
            return true;
        }
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        SubCommand sub = subs.get(args[0].toLowerCase());
        if (sub == null) {
            services.messages.send(player, "unknown-subcommand");
            return true;
        }
        sub.execute(player, Arrays.copyOfRange(args, 1, args.length));
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return List.of();
        }
        if (args.length == 1) {
            List<String> result = new ArrayList<>();
            String prefix = args[0].toLowerCase();
            for (String name : subs.keySet()) {
                if (name.startsWith(prefix)) {
                    result.add(name);
                }
            }
            return result;
        }
        SubCommand sub = subs.get(args[0].toLowerCase());
        if (sub == null) {
            return List.of();
        }
        return sub.complete(player, Arrays.copyOfRange(args, 1, args.length));
    }

    private void sendHelp(Player player) {
        services.messages.sendRaw(player, "help-header");
        for (String key : new String[]{"create", "invite", "accept", "leave",
                "disband", "rename", "info", "list"}) {
            services.messages.sendRaw(player, "help-" + key);
        }
    }
}
