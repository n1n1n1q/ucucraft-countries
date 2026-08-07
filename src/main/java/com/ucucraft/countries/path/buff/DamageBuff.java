package com.ucucraft.countries.path.buff;

import java.util.Set;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public record DamageBuff(Direction direction, double multiplier,
                         Set<DamageCause> causes, BuffScope scope) implements PathBuff {

    public enum Direction { DEALT, TAKEN }

    /** Empty cause list means every damage cause. */
    public boolean matches(DamageCause cause) {
        return causes.isEmpty() || causes.contains(cause);
    }
}
