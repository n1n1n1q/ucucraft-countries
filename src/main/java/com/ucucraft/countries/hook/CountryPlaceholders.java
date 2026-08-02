package com.ucucraft.countries.hook;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.api.ChunkPos;
import com.ucucraft.countries.era.Era;
import com.ucucraft.countries.model.Country;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Exposes country data to PlaceholderAPI. The identifier is configurable, so with
 * the default "country" the placeholders are %country%, %country_leader%, ...
 */
public final class CountryPlaceholders extends PlaceholderExpansion {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

    private final Services services;
    private final String identifier;

    public CountryPlaceholders(Services services) {
        this.services = services;
        this.identifier = services.config.placeholderIdentifier();
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getAuthor() {
        return String.join(", ", services.plugin.getPluginMeta().getAuthors());
    }

    @Override
    public String getVersion() {
        return services.plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) {
            return null;
        }
        if ("here".equalsIgnoreCase(params)) {
            return here(player);
        }
        Country country = services.countries.getByPlayer(player.getUniqueId());
        if ("has_country".equalsIgnoreCase(params)) {
            return String.valueOf(country != null);
        }
        if (country == null) {
            return text("placeholder-no-country");
        }
        return switch (params.toLowerCase()) {
            case "", "name" -> country.getName();
            case "leader" -> country.getLeaderName();
            case "members" -> String.valueOf(country.getMembers().size());
            case "member_names" -> String.join(", ", country.getMemberNames());
            case "allies" -> String.valueOf(country.getAllies().size());
            case "wars" -> String.valueOf(country.getWars().size());
            case "era" -> eraDisplay(country);
            case "era_index" -> String.valueOf(country.getEraIndex());
            case "chunks" -> String.valueOf(services.claims.count(country.getId()));
            case "chunk_limit" -> limit(country);
            default -> null;
        };
    }

    private String here(OfflinePlayer player) {
        Player online = player.getPlayer();
        if (online == null) {
            return text("claim-wilderness");
        }
        Country owner = services.countries.getById(services.claims.ownerOf(ChunkPos.of(online.getLocation())));
        return owner != null ? owner.getName() : text("claim-wilderness");
    }

    private String eraDisplay(Country country) {
        Era era = services.eras.eraOf(country);
        return era != null ? legacy(era.getDisplay()) : text("none");
    }

    private String limit(Country country) {
        int limit = services.claims.limit(country);
        return limit == Integer.MAX_VALUE ? text("claim-unlimited") : String.valueOf(limit);
    }

    private String text(String key) {
        return legacy(services.messages.text(key));
    }

    /** Placeholder consumers expect legacy colour codes, not raw MiniMessage tags. */
    private String legacy(String miniMessage) {
        return LEGACY.serialize(MINI.deserialize(miniMessage));
    }
}
