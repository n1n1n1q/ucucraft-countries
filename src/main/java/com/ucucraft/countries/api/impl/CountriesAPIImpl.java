package com.ucucraft.countries.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.api.ChunkPos;
import com.ucucraft.countries.api.CountriesAPI;
import com.ucucraft.countries.api.CountryView;
import com.ucucraft.countries.api.Relation;
import com.ucucraft.countries.model.Country;

public final class CountriesAPIImpl implements CountriesAPI {

    private final Services services;

    public CountriesAPIImpl(Services services) {
        this.services = services;
    }

    private Optional<CountryView> wrap(Country country) {
        return country == null ? Optional.empty() : Optional.of(new CountryViewImpl(services, country));
    }

    @Override
    public Optional<CountryView> getCountry(String name) {
        return wrap(services.countries.getByName(name));
    }

    @Override
    public Optional<CountryView> getCountry(UUID countryId) {
        return wrap(services.countries.getById(countryId));
    }

    @Override
    public Optional<CountryView> getCountryOf(UUID playerId) {
        return wrap(services.countries.getByPlayer(playerId));
    }

    @Override
    public Collection<CountryView> getCountries() {
        List<CountryView> views = new ArrayList<>();
        for (Country country : services.countries.allSorted()) {
            views.add(new CountryViewImpl(services, country));
        }
        return views;
    }

    @Override
    public Optional<CountryView> getCountryAt(ChunkPos pos) {
        UUID owner = services.claims.ownerOf(pos);
        return owner == null ? Optional.empty() : getCountry(owner);
    }

    @Override
    public int getClaimCount() {
        return services.claims.total();
    }

    @Override
    public Relation getRelation(UUID countryA, UUID countryB) {
        if (countryA.equals(countryB)) {
            return Relation.SELF;
        }
        Country a = services.countries.getById(countryA);
        if (a == null) {
            return Relation.NEUTRAL;
        }
        if (a.isAtWar(countryB)) {
            return Relation.WAR;
        }
        return a.isAllied(countryB) ? Relation.ALLY : Relation.NEUTRAL;
    }

    @Override
    public boolean canBuild(UUID playerId, ChunkPos pos) {
        UUID owner = services.claims.ownerOf(pos);
        if (owner == null) {
            return true;
        }
        Country country = services.countries.getByPlayer(playerId);
        return country != null && country.getId().equals(owner);
    }
}
