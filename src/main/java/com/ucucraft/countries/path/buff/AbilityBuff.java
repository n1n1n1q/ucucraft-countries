package com.ucucraft.countries.path.buff;

import java.util.List;

import org.bukkit.potion.PotionEffectType;

/** Activated buff, triggered with /country path use &lt;id&gt;. */
public record AbilityBuff(String id, String display, Target target, double radius,
                          long cooldownSeconds, boolean broadcast,
                          List<TimedPotion> effects, BuffScope scope) implements PathBuff {

    public enum Target { SELF, NEARBY, COUNTRY }

    public record TimedPotion(PotionEffectType type, int amplifier, int seconds) {
    }
}
