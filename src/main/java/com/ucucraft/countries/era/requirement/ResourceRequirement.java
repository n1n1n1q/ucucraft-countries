package com.ucucraft.countries.era.requirement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;

import com.ucucraft.countries.model.Country;
import com.ucucraft.countries.path.PathRegistry;
import com.ucucraft.countries.vault.CountryVault;
import com.ucucraft.countries.vault.VaultManager;

public final class ResourceRequirement implements Requirement {

    private final VaultManager vaults;
    private final PathRegistry paths;
    private final Map<Material, Integer> items;

    public ResourceRequirement(VaultManager vaults, PathRegistry paths, Map<Material, Integer> items) {
        this.vaults = vaults;
        this.paths = paths;
        this.items = items;
    }

    public Map<Material, Integer> getItems() {
        return items;
    }

    /** Configured amount after the country's path discount. */
    public int required(Country country, int amount) {
        return Math.max(1, (int) Math.ceil(amount * paths.eraCostMultiplier(country)));
    }

    @Override
    public String type() {
        return "resource";
    }

    @Override
    public boolean isMet(Country country) {
        CountryVault vault = vaults.get(country);
        for (Map.Entry<Material, Integer> entry : items.entrySet()) {
            if (vault.count(entry.getKey()) < required(country, entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String progress(Country country) {
        CountryVault vault = vaults.get(country);
        List<String> parts = new ArrayList<>();
        for (Map.Entry<Material, Integer> entry : items.entrySet()) {
            int needed = required(country, entry.getValue());
            int have = Math.min(vault.count(entry.getKey()), needed);
            parts.add(pretty(entry.getKey()) + " " + have + "/" + needed);
        }
        return String.join(", ", parts);
    }

    @Override
    public String descriptionKey() {
        return "era-req-resource";
    }

    @Override
    public String[] placeholders(Country country) {
        return new String[]{"items", progress(country)};
    }

    @Override
    public void consume(Country country) {
        CountryVault vault = vaults.get(country);
        for (Map.Entry<Material, Integer> entry : items.entrySet()) {
            vault.remove(entry.getKey(), required(country, entry.getValue()));
        }
    }

    static String pretty(Material material) {
        return material.name().toLowerCase().replace('_', ' ');
    }
}
