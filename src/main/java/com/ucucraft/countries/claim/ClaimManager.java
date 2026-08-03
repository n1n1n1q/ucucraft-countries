package com.ucucraft.countries.claim;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.ucucraft.countries.api.ChunkPos;
import com.ucucraft.countries.config.PluginConfig;
import com.ucucraft.countries.era.Era;
import com.ucucraft.countries.era.EraRegistry;
import com.ucucraft.countries.model.Country;

public final class ClaimManager {

    public enum Result { OK, WORLD_DISABLED, ALREADY_OWNED, TAKEN, LIMIT_REACHED, NOT_ADJACENT, WILDERNESS, NOT_OWNER }

    private final PluginConfig config;
    private final ClaimStorage storage;
    private final EraRegistry eras;
    private final Map<ChunkPos, UUID> owners = new HashMap<>();
    private final Map<UUID, Set<ChunkPos>> byCountry = new LinkedHashMap<>();

    public ClaimManager(PluginConfig config, ClaimStorage storage, EraRegistry eras) {
        this.config = config;
        this.storage = storage;
        this.eras = eras;
    }

    public void load() {
        owners.clear();
        byCountry.clear();
        for (Map.Entry<UUID, Set<ChunkPos>> entry : storage.loadAll().entrySet()) {
            byCountry.put(entry.getKey(), entry.getValue());
            for (ChunkPos pos : entry.getValue()) {
                owners.put(pos, entry.getKey());
            }
        }
    }

    public void save() {
        storage.saveAll(byCountry);
    }

    public UUID ownerOf(ChunkPos pos) {
        return owners.get(pos);
    }

    public Set<ChunkPos> claimsOf(UUID countryId) {
        return Collections.unmodifiableSet(byCountry.getOrDefault(countryId, Set.of()));
    }

    public int count(UUID countryId) {
        return byCountry.getOrDefault(countryId, Set.of()).size();
    }

    public int total() {
        return owners.size();
    }

    /** Chunk allowance of the country; {@link Integer#MAX_VALUE} when unlimited. */
    public int limit(Country country) {
        Era era = eras.byIndex(country.getEraIndex());
        int base;
        int perPlayer;

        if (era != null && era.getBaseChunkLimit() >= 0) {
            base = era.getBaseChunkLimit();
            perPlayer = era.getAdditionalChunksPerPlayer();
        } else {
            base = config.claimBaseLimit();
            if (base <= 0) {
                return Integer.MAX_VALUE;
            }
            perPlayer = config.claimPerEraBonus();
        }

        if (base <= 0) {
            return Integer.MAX_VALUE;
        }

        int playerBonus = perPlayer * country.getMembers().size();
        return base + playerBonus;
    }

    public Result claim(Country country, ChunkPos pos) {
        if (config.claimDisabledWorlds().contains(pos.world())) {
            return Result.WORLD_DISABLED;
        }
        UUID owner = owners.get(pos);
        if (country.getId().equals(owner)) {
            return Result.ALREADY_OWNED;
        }
        if (owner != null) {
            return Result.TAKEN;
        }
        Set<ChunkPos> own = byCountry.getOrDefault(country.getId(), Set.of());
        if (own.size() >= limit(country)) {
            return Result.LIMIT_REACHED;
        }
        if (config.claimRequireAdjacent() && !own.isEmpty() && !touches(own, pos)) {
            return Result.NOT_ADJACENT;
        }
        byCountry.computeIfAbsent(country.getId(), id -> new LinkedHashSet<>()).add(pos);
        owners.put(pos, country.getId());
        save();
        return Result.OK;
    }

    public Result unclaim(Country country, ChunkPos pos) {
        UUID owner = owners.get(pos);
        if (owner == null) {
            return Result.WILDERNESS;
        }
        if (!owner.equals(country.getId())) {
            return Result.NOT_OWNER;
        }
        owners.remove(pos);
        Set<ChunkPos> own = byCountry.get(country.getId());
        if (own != null) {
            own.remove(pos);
            if (own.isEmpty()) {
                byCountry.remove(country.getId());
            }
        }
        save();
        return Result.OK;
    }

    public void releaseAll(Country country) {
        Set<ChunkPos> own = byCountry.remove(country.getId());
        if (own == null) {
            return;
        }
        owners.keySet().removeAll(own);
        save();
    }

    private boolean touches(Set<ChunkPos> own, ChunkPos pos) {
        for (ChunkPos neighbour : pos.neighbours()) {
            if (own.contains(neighbour)) {
                return true;
            }
        }
        return false;
    }
}
