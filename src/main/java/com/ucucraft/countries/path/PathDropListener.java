package com.ucucraft.countries.path;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import com.ucucraft.countries.manager.CountryManager;
import com.ucucraft.countries.model.Country;
import com.ucucraft.countries.path.buff.DropBuff;

/** Applies the block yield multipliers of a path. */
public final class PathDropListener implements Listener {

    private final PathManager paths;
    private final CountryManager countries;

    public PathDropListener(PathManager paths, CountryManager countries) {
        this.paths = paths;
        this.countries = countries;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        Country country = countries.getByPlayer(player.getUniqueId());
        if (country == null || !country.hasPath()) {
            return;
        }
        Material broken = event.getBlockState().getType();
        double multiplier = 1.0;
        for (DropBuff buff : paths.active(player, country, DropBuff.class)) {
            if (buff.matches(broken)) {
                multiplier *= buff.multiplier();
            }
        }
        if (multiplier <= 1.0) {
            return;
        }
        for (Item item : event.getItems()) {
            ItemStack stack = item.getItemStack();
            int extra = extra(stack.getAmount(), multiplier);
            if (extra > 0) {
                stack.setAmount(Math.min(stack.getMaxStackSize(), stack.getAmount() + extra));
                item.setItemStack(stack);
            }
        }
    }

    /** Fractional bonuses become a chance, so a 1.25x multiplier pays out on a quarter of the drops. */
    private int extra(int amount, double multiplier) {
        double bonus = amount * (multiplier - 1.0);
        int whole = (int) bonus;
        return ThreadLocalRandom.current().nextDouble() < bonus - whole ? whole + 1 : whole;
    }
}
