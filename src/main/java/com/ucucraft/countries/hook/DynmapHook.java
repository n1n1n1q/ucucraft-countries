package com.ucucraft.countries.hook;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.api.ChunkPos;
import com.ucucraft.countries.model.Country;

/** Renders each country's claimed land as coloured outlines on the Dynmap web map. */
public final class DynmapHook {

    private static final int[] PALETTE = {
            0xE6194B, 0x3CB44B, 0x4363D8, 0xF58231, 0x911EB4,
            0x46F0F0, 0xF032E6, 0xBCF60C, 0xFABEBE, 0x008080,
            0x9A6324, 0x800000, 0x808000, 0xFFD700, 0x000075
    };

    private final Services services;
    private MarkerSet markerSet;

    public DynmapHook(Services services) {
        this.services = services;
    }

    /** Attaches to the running Dynmap plugin. Returns false if Dynmap is absent. */
    public boolean enable() {
        Plugin dynmap = Bukkit.getPluginManager().getPlugin("dynmap");
        if (!(dynmap instanceof DynmapAPI dynmapApi) || !dynmap.isEnabled()) {
            return false;
        }
        MarkerAPI markerApi = dynmapApi.getMarkerAPI();
        if (markerApi == null) {
            return false;
        }
        String id = services.config.dynmapMarkerSetId();
        markerSet = markerApi.getMarkerSet(id);
        if (markerSet == null) {
            markerSet = markerApi.createMarkerSet(id, services.config.dynmapMarkerSetLabel(), null, false);
        } else {
            markerSet.setMarkerSetLabel(services.config.dynmapMarkerSetLabel());
        }
        return markerSet != null;
    }

    /** Redraws every country's claims from current claim data. */
    public void update() {
        if (markerSet == null) {
            return;
        }
        Set<String> keep = new HashSet<>();
        for (Country country : services.countries.allSorted()) {
            Set<ChunkPos> claims = services.claims.claimsOf(country.getId());
            if (claims.isEmpty()) {
                continue;
            }
            int color = color(country);
            String label = services.config.dynmapLabelFormat().replace("{country}", country.getName());
            int index = 0;
            for (ChunkOutline.Loop loop : ChunkOutline.trace(claims)) {
                String id = country.getId() + "_" + (index++);
                keep.add(id);
                applyLoop(id, label, color, loop);
            }
        }
        for (AreaMarker marker : markerSet.getAreaMarkers()) {
            if (!keep.contains(marker.getMarkerID())) {
                marker.deleteMarker();
            }
        }
    }

    private void applyLoop(String id, String label, int color, ChunkOutline.Loop loop) {
        AreaMarker marker = markerSet.findAreaMarker(id);
        if (marker == null) {
            marker = markerSet.createAreaMarker(id, label, false, loop.world(), loop.xCorners(), loop.zCorners(), false);
        } else {
            marker.setCornerLocations(loop.xCorners(), loop.zCorners());
            marker.setLabel(label);
        }
        if (marker == null) {
            return;
        }
        marker.setLineStyle(services.config.dynmapLineWeight(), services.config.dynmapLineOpacity(), color);
        marker.setFillStyle(services.config.dynmapFillOpacity(), color);
    }

    /** Deterministic palette colour per country, stable across restarts. */
    private int color(Country country) {
        int index = Math.floorMod(country.getId().hashCode(), PALETTE.length);
        return PALETTE[index];
    }

    /** Removes the marker set from Dynmap. */
    public void disable() {
        if (markerSet != null) {
            markerSet.deleteMarkerSet();
            markerSet = null;
        }
    }
}
