package com.ucucraft.countries.path;

import java.util.List;

import com.ucucraft.countries.path.buff.PathBuff;

/** One milestone of a path: the era that unlocks it and the buffs it grants. */
public final class PathTier {

    private final String eraId;
    private final int eraIndex;
    private final String display;
    private final List<String> description;
    private final List<PathBuff> buffs;

    public PathTier(String eraId, int eraIndex, String display, List<String> description, List<PathBuff> buffs) {
        this.eraId = eraId;
        this.eraIndex = eraIndex;
        this.display = display;
        this.description = description;
        this.buffs = buffs;
    }

    public String getEraId() {
        return eraId;
    }

    public int getEraIndex() {
        return eraIndex;
    }

    public String getDisplay() {
        return display;
    }

    public List<String> getDescription() {
        return description;
    }

    public List<PathBuff> getBuffs() {
        return buffs;
    }
}
