package com.ucucraft.countries.era;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.ucucraft.countries.Services;

public final class EraGateListener implements Listener {

    private final Services services;
    private final Map<UUID, Long> lastMessage = new HashMap<>();

    public EraGateListener(Services services) {
        this.services = services;
    }

    /** True when the player may not use the material; also sends the rate-limited notice. */
    private boolean blocked(Player player, Material material) {
        if (material == null || material.isAir()) {
            return false;
        }
        if (services.eras.isAllowed(player, material)) {
            return false;
        }
        notifyLocked(player, material);
        return true;
    }

    private void notifyLocked(Player player, Material material) {
        long now = System.currentTimeMillis();
        Long last = lastMessage.get(player.getUniqueId());
        if (last != null && now - last < services.eras.gateCooldownMs()) {
            return;
        }
        lastMessage.put(player.getUniqueId(), now);
        Era required = services.eras.registry()
                .byIndex(services.eras.registry().requiredIndex(material));
        services.eraMessages.send(player, "era-locked",
                "item", material.name().toLowerCase().replace('_', ' '),
                "era", required != null ? required.getDisplay() : "?");
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!services.eras.gateEnabled("craft") || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack result = event.getRecipe().getResult();
        if (blocked(player, result.getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (!services.eras.gateEnabled("place")) {
            return;
        }
        if (blocked(event.getPlayer(), event.getBlockPlaced().getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!services.eras.gateEnabled("use") || event.getItem() == null) {
            return;
        }
        if (blocked(event.getPlayer(), event.getItem().getType())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!services.eras.gateEnabled("use") || !(event.getDamager() instanceof Player player)) {
            return;
        }
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (blocked(player, hand.getType())) {
            event.setCancelled(true);
        }
    }
}
