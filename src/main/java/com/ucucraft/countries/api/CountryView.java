package com.ucucraft.countries.api;

import java.util.Set;
import java.util.UUID;

/** Read-only snapshot of a country. All collections are unmodifiable. */
public interface CountryView {

    UUID id();

    String name();

    UUID leader();

    Set<UUID> members();

    Set<UUID> trusted();

    Set<UUID> allies();

    Set<UUID> wars();

    int eraIndex();

    /** Era id from eras.yml, or null when no eras are configured. */
    String eraId();

    /** Display name of the current era, or null when no eras are configured. */
    String eraDisplay();

    /** Epoch millis the country entered its current era. */
    long eraSince();

    /** Path id from paths.yml, or null when the country picked none. */
    String pathId();

    /** Display name of the chosen path, or null when the country picked none. */
    String pathDisplay();

    Set<ChunkPos> claims();

    /** Maximum chunks this country may own; {@link Integer#MAX_VALUE} when unlimited. */
    int claimLimit();
}
