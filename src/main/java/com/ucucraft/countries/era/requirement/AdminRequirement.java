package com.ucucraft.countries.era.requirement;

import com.ucucraft.countries.model.Country;

public final class AdminRequirement implements Requirement {

    private final String id;
    private final String display;

    public AdminRequirement(String id, String display) {
        this.id = id;
        this.display = display;
    }

    public String getId() {
        return id;
    }

    @Override
    public String type() {
        return "admin";
    }

    @Override
    public boolean isMet(Country country) {
        return country.getCompletedCriteria().contains(id);
    }

    @Override
    public String progress(Country country) {
        return isMet(country) ? "1/1" : "0/1";
    }

    @Override
    public String descriptionKey() {
        return "era-req-admin";
    }

    @Override
    public String[] placeholders(Country country) {
        return new String[]{"task", display, "id", id};
    }
}
