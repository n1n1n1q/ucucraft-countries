package com.ucucraft.countries.api;

import org.bukkit.Chunk;
import org.bukkit.Location;

/** World-qualified chunk coordinate used as the claim key. */
public record ChunkPos(String world, int x, int z) {

    public static ChunkPos of(Chunk chunk) {
        return new ChunkPos(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public static ChunkPos of(Location location) {
        return new ChunkPos(location.getWorld().getName(),
                location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    /** Parses {@link #key()}; returns null when the text is not a valid key. */
    public static ChunkPos parse(String key) {
        int zSep = key.lastIndexOf(';');
        if (zSep <= 0) {
            return null;
        }
        int xSep = key.lastIndexOf(';', zSep - 1);
        if (xSep <= 0) {
            return null;
        }
        try {
            return new ChunkPos(key.substring(0, xSep),
                    Integer.parseInt(key.substring(xSep + 1, zSep)),
                    Integer.parseInt(key.substring(zSep + 1)));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String key() {
        return world + ';' + x + ';' + z;
    }

    /** The four orthogonally touching chunks. */
    public ChunkPos[] neighbours() {
        return new ChunkPos[]{
                new ChunkPos(world, x + 1, z),
                new ChunkPos(world, x - 1, z),
                new ChunkPos(world, x, z + 1),
                new ChunkPos(world, x, z - 1)
        };
    }

    @Override
    public String toString() {
        return key();
    }
}
