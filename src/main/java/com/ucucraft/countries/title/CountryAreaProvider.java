package com.ucucraft.countries.title;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.api.ChunkPos;
import com.ucucraft.countries.model.Country;
import com.ucucraft.titles.location.Area;
import com.ucucraft.titles.location.AreaProvider;
import com.ucucraft.titles.location.AreaSound;

/** Names claimed land for the Titles plugin, which decides when and how to show it. */
public final class CountryAreaProvider implements AreaProvider {

    private static final String WILDERNESS = "wilderness";

    private final Services services;

    public CountryAreaProvider(Services services) {
        this.services = services;
    }

    /** Null when the country has no sound configured, which is the default. */
    private AreaSound sound(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        return new AreaSound(key, services.config.titleSoundVolume(), services.config.titleSoundPitch());
    }

    @Override
    public Area areaAt(Player player, Location location) {
        if (!services.config.titlesEnabled()) {
            return null;
        }
        UUID ownerId = services.claims.ownerOf(ChunkPos.of(location));
        Country owner = ownerId != null ? services.countries.getById(ownerId) : null;
        if (owner != null) {
            return new Area("country:" + owner.getId(), services.config.titleStyle(),
                    services.messages.text("title-country", "country", owner.getName()),
                    services.messages.text("subtitle-country", "country", owner.getName()),
                    services.config.titlePriority(),
                    sound(services.config.titleSound(owner.getName())));
        }
        if (!services.config.titlesWilderness()) {
            return null;
        }
        // Lowest priority: any named place another plugin knows about wins over open land.
        return new Area(WILDERNESS, services.config.titleWildernessStyle(),
                services.messages.text("title-wilderness"),
                services.messages.text("subtitle-wilderness"),
                services.config.titleWildernessPriority(),
                sound(services.config.titleWildernessSound()));
    }
}
