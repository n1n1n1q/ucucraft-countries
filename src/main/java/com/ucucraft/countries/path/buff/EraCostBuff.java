package com.ucucraft.countries.path.buff;

/** Multiplies the resource requirements of the next era. Country-wide, so it has no scope. */
public record EraCostBuff(double multiplier) implements PathBuff {

    @Override
    public BuffScope scope() {
        return BuffScope.ALWAYS;
    }
}
