package com.ucucraft.countries.path.buff;

/** A single boost granted by a path tier. */
public interface PathBuff {

    /** Conditions the player must satisfy for this buff to count. */
    BuffScope scope();
}
