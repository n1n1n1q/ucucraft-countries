package com.ucucraft.countries.command.sub;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.command.SubCommand;
import com.ucucraft.countries.config.Messages;
import com.ucucraft.countries.era.Era;
import com.ucucraft.countries.model.Country;
import com.ucucraft.countries.path.Path;
import com.ucucraft.countries.path.PathManager.AbilityResult;
import com.ucucraft.countries.path.PathManager.ChoiceResult;
import com.ucucraft.countries.path.PathTier;
import com.ucucraft.countries.path.buff.AbilityBuff;

public final class PathSub implements SubCommand {

    private final Services services;

    public PathSub(Services services) {
        this.services = services;
    }

    private Messages lang() {
        return services.pathMessages;
    }

    @Override
    public String name() {
        return "path";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!services.paths.enabled()) {
            lang().send(player, services.paths.registry().isEmpty() ? "path-none-defined" : "path-disabled");
            return;
        }
        String action = args.length > 0 ? args[0].toLowerCase() : "";
        switch (action) {
            case "" -> current(player);
            case "list" -> list(player);
            case "info" -> info(player, args);
            case "choose" -> choose(player, args);
            case "use" -> use(player, args);
            default -> lang().send(player, "usage-path");
        }
    }

    private Country requireCountry(Player player) {
        Country country = services.countries.getByPlayer(player.getUniqueId());
        if (country == null) {
            lang().send(player, "path-no-country");
        }
        return country;
    }

    private void current(Player player) {
        Country country = requireCountry(player);
        if (country == null) {
            return;
        }
        Path path = services.paths.pathOf(country);
        if (path == null) {
            lang().send(player, "path-current-none");
            return;
        }
        lang().sendRaw(player, "path-current", "path", path.getDisplay());
        for (PathTier tier : path.getTiers()) {
            boolean unlocked = tier.getEraIndex() <= country.getEraIndex();
            String era = eraDisplay(tier);
            lang().sendRaw(player, unlocked ? "path-tier-unlocked" : "path-tier-locked",
                    "tier", tier.getDisplay(), "era", era);
            if (unlocked) {
                for (String line : tier.getDescription()) {
                    lang().sendRaw(player, "path-tier-line", "text", line);
                }
            }
        }
        PathTier next = services.paths.registry().next(country);
        if (next != null) {
            lang().sendRaw(player, "path-next-unlock", "tier", next.getDisplay(), "era", eraDisplay(next));
        } else {
            lang().sendRaw(player, "path-no-more-tiers");
        }
        abilities(player, country);
    }

    private void abilities(Player player, Country country) {
        List<AbilityBuff> abilities = services.paths.abilities(country);
        if (abilities.isEmpty()) {
            return;
        }
        lang().sendRaw(player, "path-abilities-header");
        for (AbilityBuff ability : abilities) {
            long left = services.paths.cooldownLeft(country, ability);
            if (left <= 0) {
                lang().sendRaw(player, "path-ability-ready",
                        "ability", ability.display(), "id", ability.id());
            } else {
                lang().sendRaw(player, "path-ability-cooling",
                        "ability", ability.display(), "time", time(left));
            }
        }
    }

    private void list(Player player) {
        Country country = services.countries.getByPlayer(player.getUniqueId());
        String own = country != null ? country.getPathId() : null;
        lang().sendRaw(player, "path-list-header");
        for (Path path : services.paths.registry().all()) {
            boolean mine = path.getId().equalsIgnoreCase(own);
            lang().sendRaw(player, mine ? "path-list-current" : "path-list-entry",
                    "id", path.getId(), "path", path.getDisplay());
            for (String line : path.getDescription()) {
                lang().sendRaw(player, "path-list-line", "text", line);
            }
        }
        lang().sendRaw(player, "path-list-footer");
    }

    private void info(Player player, String[] args) {
        if (args.length < 2) {
            lang().send(player, "usage-path");
            return;
        }
        Path path = services.paths.registry().byId(args[1]);
        if (path == null) {
            lang().send(player, "path-unknown", "path", args[1]);
            return;
        }
        lang().sendRaw(player, "path-info-header", "path", path.getDisplay());
        for (String line : path.getDescription()) {
            lang().sendRaw(player, "path-list-line", "text", line);
        }
        Country country = services.countries.getByPlayer(player.getUniqueId());
        int era = country != null ? country.getEraIndex() : -1;
        for (PathTier tier : path.getTiers()) {
            lang().sendRaw(player, tier.getEraIndex() <= era ? "path-tier-unlocked" : "path-tier-locked",
                    "tier", tier.getDisplay(), "era", eraDisplay(tier));
            for (String line : tier.getDescription()) {
                lang().sendRaw(player, "path-tier-line", "text", line);
            }
        }
    }

    private void choose(Player player, String[] args) {
        Country country = requireCountry(player);
        if (country == null) {
            return;
        }
        if (args.length < 2) {
            lang().send(player, "usage-path");
            return;
        }
        if (services.paths.registry().leaderOnly() && !country.isLeader(player.getUniqueId())) {
            lang().send(player, "path-not-leader");
            return;
        }
        ChoiceResult result = services.paths.choose(country, args[1]);
        switch (result) {
            case DISABLED -> lang().send(player, "path-disabled");
            case UNKNOWN -> lang().send(player, "path-unknown", "path", args[1]);
            case LOCKED -> lang().send(player, "path-already-chosen",
                    "path", services.paths.pathOf(country).getDisplay());
            case OK -> {
                String display = services.paths.pathOf(country).getDisplay();
                if (services.paths.registry().announceChoice()) {
                    lang().broadcast("path-announce-chosen", "country", country.getName(), "path", display);
                } else {
                    lang().send(player, "path-chosen", "path", display);
                }
            }
        }
    }

    private void use(Player player, String[] args) {
        Country country = requireCountry(player);
        if (country == null) {
            return;
        }
        if (services.paths.abilities(country).isEmpty()) {
            lang().send(player, "ability-none");
            return;
        }
        if (args.length < 2) {
            abilities(player, country);
            return;
        }
        String id = args[1];
        AbilityBuff ability = services.paths.ability(country, id);
        AbilityResult result = services.paths.use(player, country, id);
        switch (result) {
            case UNKNOWN -> lang().send(player, "ability-unknown", "ability", id);
            case ON_COOLDOWN -> lang().send(player, "ability-cooldown", "ability", ability.display(),
                    "time", time(services.paths.cooldownLeft(country, ability)));
            case UNAVAILABLE -> lang().send(player, "ability-unavailable", "ability", ability.display());
            case OK -> {
            }
        }
    }

    private String eraDisplay(PathTier tier) {
        Era era = services.eras.registry().byId(tier.getEraId());
        return era != null ? era.getDisplay() : tier.getEraId();
    }

    private String time(long seconds) {
        long minutes = seconds / 60;
        return minutes > 0 ? minutes + "m " + seconds % 60 + "s" : seconds + "s";
    }

    @Override
    public List<String> complete(Player player, String[] args) {
        if (args.length == 1) {
            return Diplo.filter(List.of("list", "info", "choose", "use"), args[0]);
        }
        if (args.length == 2) {
            if ("use".equalsIgnoreCase(args[0])) {
                Country country = services.countries.getByPlayer(player.getUniqueId());
                List<String> ids = new ArrayList<>();
                if (country != null) {
                    for (AbilityBuff ability : services.paths.abilities(country)) {
                        ids.add(ability.id());
                    }
                }
                return Diplo.filter(ids, args[1]);
            }
            if ("info".equalsIgnoreCase(args[0]) || "choose".equalsIgnoreCase(args[0])) {
                List<String> ids = new ArrayList<>();
                for (Path path : services.paths.registry().all()) {
                    ids.add(path.getId());
                }
                return Diplo.filter(ids, args[1]);
            }
        }
        return List.of();
    }
}
