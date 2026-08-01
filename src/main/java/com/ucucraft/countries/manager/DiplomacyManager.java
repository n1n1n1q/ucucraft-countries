package com.ucucraft.countries.manager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.ucucraft.countries.config.PluginConfig;

public final class DiplomacyManager {

    public enum Kind { ALLY, PEACE }

    public static final class Offer {
        private final Kind kind;
        private final UUID from;
        private final String fromName;
        private final long expiresAt;

        Offer(Kind kind, UUID from, String fromName, long expiresAt) {
            this.kind = kind;
            this.from = from;
            this.fromName = fromName;
            this.expiresAt = expiresAt;
        }

        public Kind getKind() {
            return kind;
        }

        public UUID getFrom() {
            return from;
        }

        public String getFromName() {
            return fromName;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    private final PluginConfig config;
    // target country id -> (kind:fromId -> offer)
    private final Map<UUID, Map<String, Offer>> offers = new HashMap<>();

    public DiplomacyManager(PluginConfig config) {
        this.config = config;
    }

    private static String slot(Kind kind, UUID from) {
        return kind.name() + ":" + from;
    }

    public void offer(UUID target, Kind kind, UUID from, String fromName) {
        long expiresAt = System.currentTimeMillis() + config.diplomacyDurationSeconds() * 1000L;
        offers.computeIfAbsent(target, k -> new HashMap<>())
                .put(slot(kind, from), new Offer(kind, from, fromName, expiresAt));
    }

    public boolean has(UUID target, Kind kind, UUID from) {
        Map<String, Offer> byTarget = offers.get(target);
        if (byTarget == null) {
            return false;
        }
        Offer offer = byTarget.get(slot(kind, from));
        return offer != null && !offer.isExpired();
    }

    /** Removes and returns the matching offer (even if expired), or null if none exists. */
    public Offer take(UUID target, Kind kind, UUID from) {
        Map<String, Offer> byTarget = offers.get(target);
        if (byTarget == null) {
            return null;
        }
        Offer offer = byTarget.remove(slot(kind, from));
        if (byTarget.isEmpty()) {
            offers.remove(target);
        }
        return offer;
    }

    /** Drops every offer to or from the given country, e.g. when it disbands. */
    public void removeAllInvolving(UUID countryId) {
        offers.remove(countryId);
        Iterator<Map.Entry<UUID, Map<String, Offer>>> outer = offers.entrySet().iterator();
        while (outer.hasNext()) {
            Map<String, Offer> byTarget = outer.next().getValue();
            byTarget.values().removeIf(offer -> offer.getFrom().equals(countryId));
            if (byTarget.isEmpty()) {
                outer.remove();
            }
        }
    }

    public void sweep() {
        Iterator<Map<String, Offer>> outer = offers.values().iterator();
        while (outer.hasNext()) {
            Map<String, Offer> byTarget = outer.next();
            byTarget.values().removeIf(Offer::isExpired);
            if (byTarget.isEmpty()) {
                outer.remove();
            }
        }
    }

    public void clear() {
        offers.clear();
    }
}
