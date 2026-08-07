package com.ucucraft.countries.path.buff;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.ucucraft.countries.api.ChunkPos;
import com.ucucraft.countries.claim.ClaimManager;
import com.ucucraft.countries.model.Country;

/** Where a player stands and how their country stands, resolved once per buff pass. */
public final class BuffContext {

    private final Player player;
    private final Country country;
    private final ClaimManager claims;
    private final int undergroundY;
    private boolean resolved;
    private UUID owner;

    public BuffContext(Player player, Country country, ClaimManager claims, int undergroundY) {
        this.player = player;
        this.country = country;
        this.claims = claims;
        this.undergroundY = undergroundY;
    }

    public Player player() {
        return player;
    }

    public Country country() {
        return country;
    }

    public int undergroundY() {
        return undergroundY;
    }

    /** Country owning the chunk the player stands in, or null for wilderness. */
    public UUID owner() {
        if (!resolved) {
            owner = claims.ownerOf(ChunkPos.of(player.getLocation()));
            resolved = true;
        }
        return owner;
    }

    public boolean onOwnClaim() {
        return country.getId().equals(owner());
    }

    public boolean onForeignClaim() {
        UUID current = owner();
        return current != null && !current.equals(country.getId());
    }

    public boolean onEnemyClaim() {
        UUID current = owner();
        return current != null && country.isAtWar(current);
    }

    public boolean isNight() {
        long time = player.getWorld().getTime();
        return time >= 13000 && time < 23000;
    }
}
