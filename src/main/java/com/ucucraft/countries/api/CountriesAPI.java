package com.ucucraft.countries.api;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Public entry point of the Countries plugin.
 *
 * <p>Obtain an instance through {@link CountriesProvider#get()} or the Bukkit
 * {@code ServicesManager}. Everything returned is read-only.
 */
public interface CountriesAPI {

    Optional<CountryView> getCountry(String name);

    Optional<CountryView> getCountry(UUID countryId);

    Optional<CountryView> getCountryOf(UUID playerId);

    default Optional<CountryView> getCountryOf(Player player) {
        return getCountryOf(player.getUniqueId());
    }

    Collection<CountryView> getCountries();

    /** Country owning the chunk, or empty for wilderness. */
    Optional<CountryView> getCountryAt(ChunkPos pos);

    default Optional<CountryView> getCountryAt(Chunk chunk) {
        return getCountryAt(ChunkPos.of(chunk));
    }

    default Optional<CountryView> getCountryAt(Location location) {
        return getCountryAt(ChunkPos.of(location));
    }

    default boolean isClaimed(ChunkPos pos) {
        return getCountryAt(pos).isPresent();
    }

    /** Total chunks claimed across all countries. */
    int getClaimCount();

    Relation getRelation(UUID countryA, UUID countryB);

    default Relation getRelation(CountryView a, CountryView b) {
        return getRelation(a.id(), b.id());
    }

    /** True when the chunk is wilderness or owned by the player's own country. */
    boolean canBuild(UUID playerId, ChunkPos pos);

    default boolean canBuild(Player player, Location location) {
        return canBuild(player.getUniqueId(), ChunkPos.of(location));
    }
}
