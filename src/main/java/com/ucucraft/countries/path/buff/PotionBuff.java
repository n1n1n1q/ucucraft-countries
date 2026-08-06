package com.ucucraft.countries.path.buff;

import org.bukkit.potion.PotionEffectType;

public record PotionBuff(PotionEffectType type, int amplifier, BuffScope scope) implements PathBuff {
}
