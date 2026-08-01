package com.ucucraft.countries.command.sub;

import java.util.List;

import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.command.SubCommand;
import com.ucucraft.countries.era.Era;
import com.ucucraft.countries.era.requirement.Requirement;
import com.ucucraft.countries.model.Country;

public final class EraSub implements SubCommand {

    private final Services services;

    public EraSub(Services services) {
        this.services = services;
    }

    @Override
    public String name() {
        return "era";
    }

    @Override
    public void execute(Player player, String[] args) {
        String action = args.length > 0 ? args[0].toLowerCase() : "";
        if (services.eras.registry().isEmpty()) {
            services.eraMessages.send(player, "era-none-defined");
            return;
        }
        switch (action) {
            case "list" -> list(player, args);
            case "" -> current(player);
            case "next" -> next(player);
            case "advance" -> advance(player);
            default -> services.eraMessages.send(player, "usage-era");
        }
    }

    private Country requireCountry(Player player) {
        Country country = services.countries.getByPlayer(player.getUniqueId());
        if (country == null) {
            services.eraMessages.send(player, "era-no-country");
        }
        return country;
    }

    private void current(Player player) {
        Country country = requireCountry(player);
        if (country == null) {
            return;
        }
        Era era = services.eras.eraOf(country);
        services.eraMessages.sendRaw(player, "era-current", "era", era.getDisplay());
        next(player);
    }

    private void next(Player player) {
        Country country = requireCountry(player);
        if (country == null) {
            return;
        }
        Era next = services.eras.nextEra(country);
        if (next == null) {
            services.eraMessages.send(player, "era-max");
            return;
        }
        services.eraMessages.sendRaw(player, "era-next-header", "era", next.getDisplay());
        if (next.getRequirements().isEmpty()) {
            services.eraMessages.sendRaw(player, "era-req-none");
            return;
        }
        for (Requirement requirement : next.getRequirements()) {
            String text = requirement.describe(services.eraMessages, country);
            services.eraMessages.sendRaw(player,
                    requirement.isMet(country) ? "era-req-met" : "era-req-unmet", "text", text);
        }
    }

    private void advance(Player player) {
        Country country = requireCountry(player);
        if (country == null) {
            return;
        }
        if (!country.isLeader(player.getUniqueId())) {
            services.messages.send(player, "not-leader");
            return;
        }
        if (services.eras.nextEra(country) == null) {
            services.eraMessages.send(player, "era-max");
            return;
        }
        if (!services.eras.tryAdvance(country)) {
            services.eraMessages.send(player, "era-not-ready");
            return;
        }
        services.eraMessages.send(player, "era-advanced",
                "era", services.eras.eraOf(country).getDisplay());
    }

    private void list(Player player, String[] args) {
        List<Era> all = services.eras.registry().all();
        int pageSize = services.config.listPageSize();
        int pages = Math.max(1, (all.size() + pageSize - 1) / pageSize);
        int page = 1;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                services.eraMessages.send(player, "usage-era");
                return;
            }
        }
        if (page < 1 || page > pages) {
            services.eraMessages.send(player, "era-bad-page", "page", String.valueOf(page));
            return;
        }
        Country country = services.countries.getByPlayer(player.getUniqueId());
        int currentIndex = country != null ? country.getEraIndex() : -1;
        services.eraMessages.sendRaw(player, "era-list-header",
                "page", String.valueOf(page), "pages", String.valueOf(pages));
        int from = (page - 1) * pageSize;
        int to = Math.min(from + pageSize, all.size());
        for (int i = from; i < to; i++) {
            Era era = all.get(i);
            services.eraMessages.sendRaw(player,
                    era.getIndex() == currentIndex ? "era-list-current" : "era-list-entry",
                    "index", String.valueOf(i + 1), "era", era.getDisplay());
        }
        if (page < pages) {
            services.eraMessages.sendRaw(player, "era-list-footer");
        }
    }

    @Override
    public List<String> complete(Player player, String[] args) {
        if (args.length == 1) {
            return List.of("list", "next", "advance");
        }
        return List.of();
    }
}
