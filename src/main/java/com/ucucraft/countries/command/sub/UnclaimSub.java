package com.ucucraft.countries.command.sub;

import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.api.ChunkPos;
import com.ucucraft.countries.claim.ClaimManager.Result;
import com.ucucraft.countries.command.SubCommand;
import com.ucucraft.countries.model.Country;

public final class UnclaimSub implements SubCommand {

    private final Services services;

    public UnclaimSub(Services services) {
        this.services = services;
    }

    @Override
    public String name() {
        return "unclaim";
    }

    @Override
    public void execute(Player player, String[] args) {
        Country country = Claims.manager(services, player);
        if (country == null) {
            return;
        }
        ChunkPos pos = ChunkPos.of(player.getLocation());
        Result result = services.claims.unclaim(country, pos);
        switch (result) {
            case OK -> services.messages.send(player, "unclaim-success",
                    "x", String.valueOf(pos.x()), "z", String.valueOf(pos.z()),
                    "world", pos.world(),
                    "used", String.valueOf(services.claims.count(country.getId())),
                    "limit", Claims.limitText(services, services.claims.limit(country)));
            case WILDERNESS -> services.messages.send(player, "unclaim-wilderness");
            case NOT_OWNER -> services.messages.send(player, "unclaim-not-owner",
                    "country", Claims.ownerName(services, services.claims.ownerOf(pos)));
            default -> services.messages.send(player, "claim-failed");
        }
    }
}
