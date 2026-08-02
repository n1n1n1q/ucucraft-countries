package com.ucucraft.countries.command.sub;

import org.bukkit.entity.Player;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.api.ChunkPos;
import com.ucucraft.countries.claim.ClaimManager.Result;
import com.ucucraft.countries.command.SubCommand;
import com.ucucraft.countries.model.Country;

public final class ClaimSub implements SubCommand {

    private final Services services;

    public ClaimSub(Services services) {
        this.services = services;
    }

    @Override
    public String name() {
        return "claim";
    }

    @Override
    public void execute(Player player, String[] args) {
        Country country = Claims.manager(services, player);
        if (country == null) {
            return;
        }
        ChunkPos pos = ChunkPos.of(player.getLocation());
        Result result = services.claims.claim(country, pos);
        switch (result) {
            case OK -> services.messages.send(player, "claim-success",
                    "x", String.valueOf(pos.x()), "z", String.valueOf(pos.z()),
                    "world", pos.world(),
                    "used", String.valueOf(services.claims.count(country.getId())),
                    "limit", Claims.limitText(services, services.claims.limit(country)));
            case WORLD_DISABLED -> services.messages.send(player, "claim-world-disabled", "world", pos.world());
            case ALREADY_OWNED -> services.messages.send(player, "claim-already-owned");
            case TAKEN -> services.messages.send(player, "claim-taken",
                    "country", Claims.ownerName(services, services.claims.ownerOf(pos)));
            case LIMIT_REACHED -> services.messages.send(player, "claim-limit-reached",
                    "limit", Claims.limitText(services, services.claims.limit(country)));
            case NOT_ADJACENT -> services.messages.send(player, "claim-not-adjacent");
            default -> services.messages.send(player, "claim-failed");
        }
    }
}
