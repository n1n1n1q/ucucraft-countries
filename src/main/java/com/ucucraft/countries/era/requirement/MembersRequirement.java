package com.ucucraft.countries.era.requirement;

import com.ucucraft.countries.model.Country;

public final class MembersRequirement implements Requirement {

    private final int amount;

    public MembersRequirement(int amount) {
        this.amount = amount;
    }

    @Override
    public String type() {
        return "members";
    }

    @Override
    public boolean isMet(Country country) {
        return country.getMembers().size() >= amount;
    }

    @Override
    public String progress(Country country) {
        return Math.min(country.getMembers().size(), amount) + "/" + amount;
    }

    @Override
    public String descriptionKey() {
        return "era-req-members";
    }

    @Override
    public String[] placeholders(Country country) {
        return new String[]{"amount", String.valueOf(amount), "progress", progress(country)};
    }
}
