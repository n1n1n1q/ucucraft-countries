package com.ucucraft.countries.era;

import java.util.List;

import com.ucucraft.countries.era.requirement.Requirement;

public final class Era {

    private final String id;
    private final int index;
    private final String display;
    private final List<Requirement> requirements;
    private final int baseChunkLimit;
    private final int additionalChunksPerPlayer;

    public Era(String id, int index, String display, List<Requirement> requirements,
               int baseChunkLimit, int additionalChunksPerPlayer) {
        this.id = id;
        this.index = index;
        this.display = display;
        this.requirements = requirements;
        this.baseChunkLimit = baseChunkLimit;
        this.additionalChunksPerPlayer = additionalChunksPerPlayer;
    }

    public String getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    public String getDisplay() {
        return display;
    }

    public List<Requirement> getRequirements() {
        return requirements;
    }

    public int getBaseChunkLimit() {
        return baseChunkLimit;
    }

    public int getAdditionalChunksPerPlayer() {
        return additionalChunksPerPlayer;
    }
}
