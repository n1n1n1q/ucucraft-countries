package com.ucucraft.countries.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.era.Era;
import com.ucucraft.countries.era.requirement.AdminRequirement;
import com.ucucraft.countries.era.requirement.Requirement;
import com.ucucraft.countries.model.Country;
import com.ucucraft.countries.path.Path;

public final class AdminCommand implements TabExecutor {

    private final Services services;

    public AdminCommand(Services services) {
        this.services = services;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            services.eraMessages.send(sender, "admin-usage");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "complete" -> criteria(sender, args, true);
            case "revoke" -> criteria(sender, args, false);
            case "setera" -> setEra(sender, args);
            case "setpath" -> setPath(sender, args);
            case "reload" -> reload(sender);
            default -> services.eraMessages.send(sender, "admin-usage");
        }
        return true;
    }

    private boolean denied(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return false;
        }
        services.eraMessages.send(sender, "admin-no-permission");
        return true;
    }

    private Country country(CommandSender sender, String name) {
        Country country = services.countries.getByName(name);
        if (country == null) {
            services.eraMessages.send(sender, "admin-country-not-found", "country", name);
        }
        return country;
    }

    private void criteria(CommandSender sender, String[] args, boolean complete) {
        if (denied(sender, "countries.admin.criteria")) {
            return;
        }
        if (args.length < 3) {
            services.eraMessages.send(sender, "admin-usage");
            return;
        }
        Country target = country(sender, NameArgs.join(args, 1, args.length - 1));
        if (target == null) {
            return;
        }
        String id = args[args.length - 1];
        if (complete) {
            if (!hasCriteria(target, id)) {
                services.eraMessages.send(sender, "admin-criteria-not-found", "criteria", id);
                return;
            }
            if (!target.getCompletedCriteria().add(id)) {
                services.eraMessages.send(sender, "admin-criteria-already");
                return;
            }
            services.countries.save();
            services.eraMessages.send(sender, "admin-criteria-completed",
                    "criteria", id, "country", target.getName());
            services.eras.checkAuto(target);
        } else {
            if (!target.getCompletedCriteria().remove(id)) {
                services.eraMessages.send(sender, "admin-criteria-not-found", "criteria", id);
                return;
            }
            services.countries.save();
            services.eraMessages.send(sender, "admin-criteria-revoked",
                    "criteria", id, "country", target.getName());
        }
    }

    private boolean hasCriteria(Country country, String id) {
        Era next = services.eras.nextEra(country);
        if (next == null) {
            return false;
        }
        for (Requirement requirement : next.getRequirements()) {
            if (requirement instanceof AdminRequirement admin && admin.getId().equalsIgnoreCase(id)) {
                return true;
            }
        }
        return false;
    }

    private void setEra(CommandSender sender, String[] args) {
        if (denied(sender, "countries.admin.setera")) {
            return;
        }
        if (args.length < 3) {
            services.eraMessages.send(sender, "admin-usage");
            return;
        }
        Country target = country(sender, NameArgs.join(args, 1, args.length - 1));
        if (target == null) {
            return;
        }
        String eraId = args[args.length - 1];
        Era era = services.eras.registry().byId(eraId);
        if (era == null) {
            services.eraMessages.send(sender, "admin-era-not-found", "era", eraId);
            return;
        }
        target.setEraIndex(era.getIndex());
        target.setEraSince(System.currentTimeMillis());
        services.countries.save();
        services.eraMessages.send(sender, "admin-era-set",
                "country", target.getName(), "era", era.getDisplay());
    }

    private void setPath(CommandSender sender, String[] args) {
        if (denied(sender, "countries.admin.setpath")) {
            return;
        }
        if (args.length < 3) {
            services.eraMessages.send(sender, "admin-usage");
            return;
        }
        Country target = country(sender, NameArgs.join(args, 1, args.length - 1));
        if (target == null) {
            return;
        }
        String pathId = args[args.length - 1];
        if ("none".equalsIgnoreCase(pathId)) {
            services.paths.set(target, null);
            services.pathMessages.send(sender, "admin-path-cleared", "country", target.getName());
            return;
        }
        Path path = services.paths.registry().byId(pathId);
        if (path == null) {
            services.pathMessages.send(sender, "admin-path-not-found", "path", pathId);
            return;
        }
        services.paths.set(target, path.getId());
        services.pathMessages.send(sender, "admin-path-set",
                "country", target.getName(), "path", path.getDisplay());
    }

    private void reload(CommandSender sender) {
        if (denied(sender, "countries.admin.reload")) {
            return;
        }
        services.plugin.reloadConfig();
        services.eras.registry().load();
        services.paths.registry().load(services.eras.registry());
        services.messages.load(services.config.language(), services.config.prefix());
        services.eraMessages.load(services.config.language(), services.config.eraPrefix());
        services.pathMessages.load(services.config.language(), services.config.pathPrefix());
        services.eraMessages.send(sender, "admin-reloaded");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("complete", "revoke", "setera", "setpath", "reload");
        }
        if (args.length < 2 || "reload".equalsIgnoreCase(args[0])) {
            return List.of();
        }
        // The country name may span several arguments, so offer both the next word of a name
        // and the value list once the words before the last one already name a country.
        List<String> names = new ArrayList<>();
        for (Country country : services.countries.allSorted()) {
            names.add(country.getName());
        }
        List<String> suggestions = new ArrayList<>(NameArgs.completeName(names, args, 1));
        Country target = services.countries.getByName(NameArgs.join(args, 1, args.length - 1));
        if (target != null) {
            for (String value : values(args[0], target)) {
                if (value.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                    suggestions.add(value);
                }
            }
        }
        return suggestions;
    }

    private List<String> values(String action, Country target) {
        List<String> ids = new ArrayList<>();
        switch (action.toLowerCase()) {
            case "setera" -> {
                for (Era era : services.eras.registry().all()) {
                    ids.add(era.getId());
                }
            }
            case "setpath" -> {
                ids.add("none");
                for (Path path : services.paths.registry().all()) {
                    ids.add(path.getId());
                }
            }
            case "complete" -> ids.addAll(criteriaIds(target));
            case "revoke" -> ids.addAll(target.getCompletedCriteria());
            default -> {
            }
        }
        return ids;
    }

    private List<String> criteriaIds(Country country) {
        List<String> ids = new ArrayList<>();
        Era next = services.eras.nextEra(country);
        if (next != null) {
            for (Requirement requirement : next.getRequirements()) {
                if (requirement instanceof AdminRequirement admin) {
                    ids.add(admin.getId());
                }
            }
        }
        return ids;
    }
}
