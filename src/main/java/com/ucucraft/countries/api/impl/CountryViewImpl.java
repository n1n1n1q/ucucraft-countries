package com.ucucraft.countries.api.impl;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.api.ChunkPos;
import com.ucucraft.countries.api.CountryView;
import com.ucucraft.countries.era.Era;
import com.ucucraft.countries.model.Country;
import com.ucucraft.countries.path.Path;

final class CountryViewImpl implements CountryView {

    private final Services services;
    private final Country country;

    CountryViewImpl(Services services, Country country) {
        this.services = services;
        this.country = country;
    }

    @Override
    public UUID id() {
        return country.getId();
    }

    @Override
    public String name() {
        return country.getName();
    }

    @Override
    public UUID leader() {
        return country.getLeader();
    }

    @Override
    public Set<UUID> members() {
        return Collections.unmodifiableSet(country.getMembers());
    }

    @Override
    public Set<UUID> trusted() {
        return Collections.unmodifiableSet(country.getTrusted());
    }

    @Override
    public Set<UUID> allies() {
        return Collections.unmodifiableSet(country.getAllies());
    }

    @Override
    public Set<UUID> wars() {
        return Collections.unmodifiableSet(country.getWars());
    }

    @Override
    public int eraIndex() {
        return country.getEraIndex();
    }

    @Override
    public String eraId() {
        Era era = services.eras.eraOf(country);
        return era != null ? era.getId() : null;
    }

    @Override
    public String eraDisplay() {
        Era era = services.eras.eraOf(country);
        return era != null ? era.getDisplay() : null;
    }

    @Override
    public long eraSince() {
        return country.getEraSince();
    }

    @Override
    public String pathId() {
        return country.getPathId();
    }

    @Override
    public String pathDisplay() {
        Path path = services.paths.pathOf(country);
        return path != null ? path.getDisplay() : null;
    }

    @Override
    public Set<ChunkPos> claims() {
        return services.claims.claimsOf(country.getId());
    }

    @Override
    public int claimLimit() {
        return services.claims.limit(country);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CountryView view && view.id().equals(country.getId());
    }

    @Override
    public int hashCode() {
        return country.getId().hashCode();
    }

    @Override
    public String toString() {
        return "CountryView[" + country.getName() + "]";
    }
}
