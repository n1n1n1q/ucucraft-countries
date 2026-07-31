package com.ucucraft.countries.command.sub;

import java.util.List;

import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.command.SubCommand;
import com.ucucraft.countries.model.Country;

public final class ListSub implements SubCommand {

    private final Services services;

    public ListSub(Services services) {
        this.services = services;
    }

    @Override
    public String name() {
        return "list";
    }

    @Override
    public void execute(Player player, String[] args) {
        List<Country> all = services.countries.allSorted();
        if (all.isEmpty()) {
            services.messages.send(player, "list-empty");
            return;
        }
        int pageSize = services.config.listPageSize();
        int pages = services.countries.pageCount(pageSize);
        int page = 1;
        if (args.length >= 1) {
            try {
                page = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                services.messages.send(player, "usage-list");
                return;
            }
        }
        if (page < 1 || page > pages) {
            services.messages.send(player, "list-bad-page", "page", String.valueOf(page));
            return;
        }
        services.messages.sendRaw(player, "list-header",
                "page", String.valueOf(page), "pages", String.valueOf(pages));
        int from = (page - 1) * pageSize;
        int to = Math.min(from + pageSize, all.size());
        for (int i = from; i < to; i++) {
            Country country = all.get(i);
            services.messages.sendRaw(player, "list-entry",
                    "country", country.getName(),
                    "members", String.valueOf(country.getMembers().size()));
        }
        if (page < pages) {
            services.messages.sendRaw(player, "list-footer");
        }
    }

    @Override
    public List<String> complete(Player player, String[] args) {
        return List.of();
    }
}
