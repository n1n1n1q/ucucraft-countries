package com.ucucraft.countries.command.sub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.command.SubCommand;
import com.ucucraft.countries.model.Country;
import com.ucucraft.countries.vault.CountryVault;
import com.ucucraft.countries.vault.VaultListener;

public final class VaultSub implements SubCommand {

    private final Services services;
    private final VaultListener gui;

    public VaultSub(Services services, VaultListener gui) {
        this.services = services;
        this.gui = gui;
    }

    @Override
    public String name() {
        return "vault";
    }

    @Override
    public void execute(Player player, String[] args) {
        Country country = services.countries.getByPlayer(player.getUniqueId());
        if (country == null) {
            services.messages.send(player, "not-in-country");
            return;
        }
        String action = args.length > 0 ? args[0].toLowerCase() : "";
        switch (action) {
            case "" -> gui.open(player, country, 0);
            case "deposit" -> deposit(player, country, args);
            case "withdraw" -> withdraw(player, country, args);
            case "list" -> list(player, country, args);
            case "trust" -> trust(player, country, args, true);
            case "untrust" -> trust(player, country, args, false);
            case "trusted" -> trusted(player, country);
            default -> openPage(player, country, action);
        }
    }

    private void openPage(Player player, Country country, String raw) {
        int page;
        try {
            page = Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            services.eraMessages.send(player, "usage-vault");
            return;
        }
        if (page < 1 || page > services.vaults.pages()) {
            services.eraMessages.send(player, "vault-bad-page", "page", String.valueOf(page));
            return;
        }
        gui.open(player, country, page - 1);
    }

    private void deposit(Player player, Country country, String[] args) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType().isAir()) {
            services.eraMessages.send(player, "vault-empty-hand");
            return;
        }
        int amount = hand.getAmount();
        if (args.length >= 2 && !"all".equalsIgnoreCase(args[1])) {
            try {
                amount = Math.min(amount, Math.max(1, Integer.parseInt(args[1])));
            } catch (NumberFormatException e) {
                services.eraMessages.send(player, "usage-vault");
                return;
            }
        }
        ItemStack toStore = hand.clone();
        toStore.setAmount(amount);

        CountryVault vault = services.vaults.get(country);
        int leftover = vault.add(toStore);
        int stored = amount - leftover;
        if (stored > 0) {
            hand.setAmount(hand.getAmount() - stored);
            player.getInventory().setItemInMainHand(hand.getAmount() > 0 ? hand : null);
            services.vaults.save();
            services.eraMessages.send(player, "vault-deposited",
                    "amount", String.valueOf(stored), "item", pretty(toStore.getType()));
            services.eras.checkAuto(country);
        }
        if (leftover > 0) {
            services.eraMessages.send(player, "vault-full", "amount", String.valueOf(leftover));
        }
    }

    private void withdraw(Player player, Country country, String[] args) {
        if (!country.canWithdraw(player.getUniqueId())) {
            services.eraMessages.send(player, "vault-no-withdraw");
            return;
        }
        if (args.length < 2) {
            services.eraMessages.send(player, "usage-vault-withdraw");
            return;
        }
        Material material = Material.matchMaterial(args[1]);
        if (material == null) {
            services.eraMessages.send(player, "vault-bad-material", "item", args[1]);
            return;
        }
        int amount = material.getMaxStackSize();
        if (args.length >= 3) {
            try {
                amount = Math.max(1, Integer.parseInt(args[2]));
            } catch (NumberFormatException e) {
                services.eraMessages.send(player, "usage-vault-withdraw");
                return;
            }
        }
        CountryVault vault = services.vaults.get(country);
        int available = Math.min(vault.count(material), amount);
        if (available <= 0) {
            services.eraMessages.send(player, "vault-nothing", "item", pretty(material));
            return;
        }
        int taken = vault.remove(material, available);
        ItemStack stack = new ItemStack(material, taken);
        Map<Integer, ItemStack> rejected = player.getInventory().addItem(stack);
        int returned = 0;
        for (ItemStack leftover : rejected.values()) {
            returned += leftover.getAmount();
            vault.add(leftover);
        }
        services.vaults.save();
        if (taken - returned > 0) {
            services.eraMessages.send(player, "vault-withdrawn",
                    "amount", String.valueOf(taken - returned), "item", pretty(material));
        }
        if (returned > 0) {
            services.eraMessages.send(player, "vault-inventory-full");
        }
    }

    private void list(Player player, Country country, String[] args) {
        Map<Material, Integer> summary = services.vaults.get(country).summary();
        if (summary.isEmpty()) {
            services.eraMessages.send(player, "vault-list-empty");
            return;
        }
        List<Map.Entry<Material, Integer>> entries = new ArrayList<>(summary.entrySet());
        entries.sort((a, b) -> b.getValue() - a.getValue());
        int pageSize = services.config.listPageSize();
        int pages = Math.max(1, (entries.size() + pageSize - 1) / pageSize);
        int page = 1;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                services.eraMessages.send(player, "usage-vault");
                return;
            }
        }
        if (page < 1 || page > pages) {
            services.eraMessages.send(player, "vault-bad-page", "page", String.valueOf(page));
            return;
        }
        services.eraMessages.sendRaw(player, "vault-list-header",
                "country", country.getName(),
                "page", String.valueOf(page), "pages", String.valueOf(pages));
        int from = (page - 1) * pageSize;
        int to = Math.min(from + pageSize, entries.size());
        for (int i = from; i < to; i++) {
            Map.Entry<Material, Integer> entry = entries.get(i);
            services.eraMessages.sendRaw(player, "vault-list-entry",
                    "item", pretty(entry.getKey()), "amount", String.valueOf(entry.getValue()));
        }
    }

    private void trust(Player player, Country country, String[] args, boolean add) {
        if (!country.isLeader(player.getUniqueId())) {
            services.messages.send(player, "not-leader");
            return;
        }
        if (args.length < 2) {
            services.eraMessages.send(player, "usage-vault-trust");
            return;
        }
        UUID uuid = memberByName(country, args[1]);
        if (uuid == null) {
            services.eraMessages.send(player, "vault-trusted-not-member");
            return;
        }
        if (add) {
            if (!country.getTrusted().add(uuid)) {
                services.eraMessages.send(player, "vault-trusted-already");
                return;
            }
        } else if (!country.getTrusted().remove(uuid)) {
            services.eraMessages.send(player, "vault-trusted-not");
            return;
        }
        services.countries.save();
        services.eraMessages.send(player, add ? "vault-trusted-added" : "vault-trusted-removed",
                "player", args[1]);
    }

    private void trusted(Player player, Country country) {
        if (country.getTrusted().isEmpty()) {
            services.eraMessages.send(player, "vault-trusted-none");
            return;
        }
        List<String> names = new ArrayList<>();
        for (UUID uuid : country.getTrusted()) {
            String name = Bukkit.getOfflinePlayer(uuid).getName();
            names.add(name != null ? name : uuid.toString());
        }
        services.eraMessages.send(player, "vault-trusted-list", "players", String.join(", ", names));
    }

    private UUID memberByName(Country country, String name) {
        for (UUID uuid : country.getMembers()) {
            OfflinePlayer member = Bukkit.getOfflinePlayer(uuid);
            if (name.equalsIgnoreCase(member.getName())) {
                return uuid;
            }
        }
        return null;
    }

    private String pretty(Material material) {
        return material.name().toLowerCase().replace('_', ' ');
    }

    @Override
    public List<String> complete(Player player, String[] args) {
        if (args.length == 1) {
            return List.of("deposit", "withdraw", "list", "trust", "untrust", "trusted");
        }
        Country country = services.countries.getByPlayer(player.getUniqueId());
        if (args.length == 2 && country != null) {
            if ("withdraw".equalsIgnoreCase(args[0])) {
                List<String> materials = new ArrayList<>();
                for (Material material : services.vaults.get(country).summary().keySet()) {
                    materials.add(material.name().toLowerCase());
                }
                return materials;
            }
            if ("trust".equalsIgnoreCase(args[0]) || "untrust".equalsIgnoreCase(args[0])) {
                return country.getMemberNames();
            }
        }
        return List.of();
    }
}
