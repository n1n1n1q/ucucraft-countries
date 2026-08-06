package com.ucucraft.countries.path.buff;

import com.ucucraft.countries.model.Country;

/** Extra chunk allowance on top of the era limit. Country-wide, so it has no scope. */
public record ClaimBuff(int flat, int perMember) implements PathBuff {

    @Override
    public BuffScope scope() {
        return BuffScope.ALWAYS;
    }

    public int bonus(Country country) {
        return flat + perMember * country.getMembers().size();
    }
}
