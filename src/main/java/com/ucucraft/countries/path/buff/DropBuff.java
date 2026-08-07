package com.ucucraft.countries.path.buff;

import java.util.Set;

import org.bukkit.Material;

public record DropBuff(double multiplier, Set<Material> materials, BuffScope scope) implements PathBuff {

    /** Empty material list means every block. */
    public boolean matches(Material broken) {
        return materials.isEmpty() || materials.contains(broken);
    }
}
