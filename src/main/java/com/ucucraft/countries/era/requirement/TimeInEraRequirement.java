package com.ucucraft.countries.era.requirement;

import java.util.concurrent.TimeUnit;

import com.ucucraft.countries.model.Country;

public final class TimeInEraRequirement implements Requirement {

    private final long hours;

    public TimeInEraRequirement(long hours) {
        this.hours = hours;
    }

    private long elapsedHours(Country country) {
        return TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - country.getEraSince());
    }

    @Override
    public String type() {
        return "time_in_era";
    }

    @Override
    public boolean isMet(Country country) {
        return elapsedHours(country) >= hours;
    }

    @Override
    public String progress(Country country) {
        return Math.min(elapsedHours(country), hours) + "/" + hours + "h";
    }

    @Override
    public String descriptionKey() {
        return "era-req-time";
    }

    @Override
    public String[] placeholders(Country country) {
        return new String[]{"hours", String.valueOf(hours), "progress", progress(country)};
    }
}
