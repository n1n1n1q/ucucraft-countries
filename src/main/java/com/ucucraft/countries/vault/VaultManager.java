package com.ucucraft.countries.vault;

import java.util.HashMap;
import java.util.Map;

import com.ucucraft.countries.config.PluginConfig;
import com.ucucraft.countries.model.Country;

public final class VaultManager {

    private final PluginConfig config;
    private final VaultStorage storage;
    private final Map<String, CountryVault> vaults = new HashMap<>();

    public VaultManager(PluginConfig config, VaultStorage storage) {
        this.config = config;
        this.storage = storage;
    }

    public int slotsPerPage() {
        return config.vaultRows() * 9;
    }

    public int pages() {
        return config.vaultPages();
    }

    public int size() {
        return slotsPerPage() * pages();
    }

    public void load() {
        vaults.clear();
        vaults.putAll(storage.loadAll(size()));
    }

    public void save() {
        storage.saveAll(vaults);
    }

    public CountryVault get(Country country) {
        return vaults.computeIfAbsent(country.getId().toString(), k -> new CountryVault(size()));
    }

    public void remove(Country country) {
        vaults.remove(country.getId().toString());
    }
}
