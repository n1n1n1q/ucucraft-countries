package com.ucucraft.countries.era;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import com.ucucraft.countries.config.Messages;
import com.ucucraft.countries.era.requirement.AdminRequirement;
import com.ucucraft.countries.era.requirement.Requirement;
import com.ucucraft.countries.manager.CountryManager;
import com.ucucraft.countries.model.Country;

public final class EraManager {

    private final EraRegistry registry;
    private final CountryManager countries;
    private final Messages lang;

    public EraManager(EraRegistry registry, CountryManager countries, Messages lang) {
        this.registry = registry;
        this.countries = countries;
        this.lang = lang;
    }

    public EraRegistry registry() {
        return registry;
    }

    public Era eraOf(Country country) {
        return registry.byIndex(country.getEraIndex());
    }

    public int indexOf(Player player) {
        Country country = countries.getByPlayer(player.getUniqueId());
        return country != null ? country.getEraIndex() : 0;
    }

    public Era nextEra(Country country) {
        int next = country.getEraIndex() + 1;
        return next < registry.size() ? registry.byIndex(next) : null;
    }

    public boolean isAllowed(Player player, Material material) {
        return indexOf(player) >= registry.requiredIndex(material);
    }

    public boolean autoAdvance() {
        return registry.config().getBoolean("settings.auto-advance", true);
    }

    public boolean broadcastEnabled() {
        return registry.config().getBoolean("settings.broadcast", true);
    }

    public boolean gateEnabled(String what) {
        return registry.config().getBoolean("settings.gate." + what, true);
    }

    public long gateCooldownMs() {
        return registry.config().getLong("settings.gate.message-cooldown-ms", 2000);
    }

    /** Era index a freshly created country starts at. */
    public int startingIndex() {
        if (registry.isEmpty()) {
            return 0;
        }
        String mode = registry.config().getString("settings.starting-era.mode", "relative");
        if ("fixed".equalsIgnoreCase(mode)) {
            Era era = registry.byId(registry.config().getString("settings.starting-era.fixed", ""));
            return era != null ? era.getIndex() : 0;
        }
        if (!"relative".equalsIgnoreCase(mode)) {
            return 0;
        }
        List<Country> all = countries.allSorted();
        if (all.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (Country country : all) {
            sum += country.getEraIndex();
        }
        int offset = registry.config().getInt("settings.starting-era.offset", 3);
        int start = (int) Math.round(sum / all.size()) - offset;
        return Math.max(0, Math.min(start, registry.size() - 1));
    }

    public List<Requirement> unmet(Country country) {
        Era next = nextEra(country);
        List<Requirement> list = new ArrayList<>();
        if (next == null) {
            return list;
        }
        for (Requirement requirement : next.getRequirements()) {
            if (!requirement.isMet(country)) {
                list.add(requirement);
            }
        }
        return list;
    }

    public boolean isReady(Country country) {
        return nextEra(country) != null && unmet(country).isEmpty();
    }

    /** Advances when every requirement is met. Returns true when the country ascended. */
    public boolean tryAdvance(Country country) {
        Era next = nextEra(country);
        if (next == null || !unmet(country).isEmpty()) {
            return false;
        }
        for (Requirement requirement : next.getRequirements()) {
            requirement.consume(country);
            if (requirement instanceof AdminRequirement admin) {
                country.getCompletedCriteria().remove(admin.getId());
            }
        }
        country.setEraIndex(next.getIndex());
        country.setEraSince(System.currentTimeMillis());
        countries.save();
        if (broadcastEnabled()) {
            lang.broadcast("era-ascended-broadcast",
                    "country", country.getName(), "era", next.getDisplay());
        }
        return true;
    }

    /** Re-checks ascension after a deposit or an admin approval. */
    public void checkAuto(Country country) {
        if (autoAdvance()) {
            tryAdvance(country);
        }
    }
}
