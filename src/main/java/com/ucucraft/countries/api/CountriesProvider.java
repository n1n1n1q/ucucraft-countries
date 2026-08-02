package com.ucucraft.countries.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

/** Convenience accessor for the registered {@link CountriesAPI} service. */
public final class CountriesProvider {

    private CountriesProvider() {
    }

    /** Returns the API, or null when the Countries plugin is not enabled. */
    public static CountriesAPI get() {
        RegisteredServiceProvider<CountriesAPI> registration =
                Bukkit.getServicesManager().getRegistration(CountriesAPI.class);
        return registration != null ? registration.getProvider() : null;
    }
}
