package com.ucucraft.countries.manager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import com.ucucraft.countries.config.PluginConfig;
import com.ucucraft.countries.model.Country;
import com.ucucraft.countries.storage.CountryStorage;

public final class CountryManager {

    public enum NameStatus { OK, TOO_SHORT, TOO_LONG, INVALID, TAKEN }

    private final PluginConfig config;
    private final CountryStorage storage;
    private final Map<String, Country> byKey = new HashMap<>();
    private final Map<UUID, Country> byId = new HashMap<>();
    private final Map<UUID, String> playerIndex = new HashMap<>();

    public CountryManager(PluginConfig config, CountryStorage storage) {
        this.config = config;
        this.storage = storage;
    }

    public void load() {
        byKey.clear();
        byId.clear();
        playerIndex.clear();
        for (Country country : storage.loadAll()) {
            index(country);
        }
    }

    public void save() {
        storage.saveAll(byKey.values());
    }

    private void index(Country country) {
        byKey.put(country.key(), country);
        byId.put(country.getId(), country);
        for (UUID member : country.getMembers()) {
            playerIndex.put(member, country.key());
        }
    }

    public Country getByName(String name) {
        return byKey.get(name.toLowerCase());
    }

    public Country getById(UUID id) {
        return byId.get(id);
    }

    public Country getByPlayer(UUID uuid) {
        String key = playerIndex.get(uuid);
        return key != null ? byKey.get(key) : null;
    }

    public boolean hasCountry(UUID uuid) {
        return playerIndex.containsKey(uuid);
    }

    public NameStatus checkName(String name) {
        if (name.length() < config.nameMinLength()) {
            return NameStatus.TOO_SHORT;
        }
        if (name.length() > config.nameMaxLength()) {
            return NameStatus.TOO_LONG;
        }
        if (!Pattern.matches(config.namePattern(), name)) {
            return NameStatus.INVALID;
        }
        if (byKey.containsKey(name.toLowerCase())) {
            return NameStatus.TAKEN;
        }
        return NameStatus.OK;
    }

    public Country create(String name, UUID leader, int startingEra) {
        Country country = new Country(name, leader);
        country.setEraIndex(startingEra);
        index(country);
        save();
        return country;
    }

    public void rename(Country country, String newName) {
        byKey.remove(country.key());
        country.setName(newName);
        byKey.put(country.key(), country);
        for (UUID member : country.getMembers()) {
            playerIndex.put(member, country.key());
        }
        save();
    }

    public void disband(Country country) {
        for (UUID member : country.getMembers()) {
            playerIndex.remove(member);
        }
        UUID id = country.getId();
        for (Country other : byKey.values()) {
            other.getAllies().remove(id);
            other.getWars().remove(id);
        }
        byKey.remove(country.key());
        byId.remove(id);
        save();
    }

    public void addMember(Country country, UUID uuid) {
        country.addMember(uuid);
        playerIndex.put(uuid, country.key());
        save();
    }

    public void removeMember(Country country, UUID uuid) {
        country.removeMember(uuid);
        playerIndex.remove(uuid);
        save();
    }

    public void ally(Country a, Country b) {
        a.getAllies().add(b.getId());
        b.getAllies().add(a.getId());
        save();
    }

    public void unally(Country a, Country b) {
        a.getAllies().remove(b.getId());
        b.getAllies().remove(a.getId());
        save();
    }

    public void declareWar(Country a, Country b) {
        a.getWars().add(b.getId());
        b.getWars().add(a.getId());
        save();
    }

    public void makePeace(Country a, Country b) {
        a.getWars().remove(b.getId());
        b.getWars().remove(a.getId());
        save();
    }

    public List<Country> allSorted() {
        List<Country> list = new ArrayList<>(byKey.values());
        list.sort(Comparator.comparing(c -> c.getName().toLowerCase()));
        return list;
    }

    public int pageCount(int pageSize) {
        int total = byKey.size();
        if (total == 0) {
            return 1;
        }
        return (total + pageSize - 1) / pageSize;
    }
}
