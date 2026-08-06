package com.ucucraft.countries.path;

import java.util.List;

public final class Path {

    private final String id;
    private final String display;
    private final List<String> description;
    private final List<PathTier> tiers;

    public Path(String id, String display, List<String> description, List<PathTier> tiers) {
        this.id = id;
        this.display = display;
        this.description = description;
        this.tiers = tiers;
    }

    public String getId() {
        return id;
    }

    public String getDisplay() {
        return display;
    }

    public List<String> getDescription() {
        return description;
    }

    /** Tiers ordered by the era that unlocks them. */
    public List<PathTier> getTiers() {
        return tiers;
    }
}
