package com.ucucraft.countries.vault;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import com.ucucraft.countries.Services;
import com.ucucraft.countries.model.Country;

public final class VaultListener implements Listener {

    private final Services services;

    public VaultListener(Services services) {
        this.services = services;
    }

    private VaultGui guiOf(Inventory inventory) {
        return inventory != null && inventory.getHolder() instanceof VaultGui gui ? gui : null;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        VaultGui gui = guiOf(event.getView().getTopInventory());
        if (gui == null || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Double-click gathers matching items from every open inventory, vault included.
        if (!gui.canWithdraw() && event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
            return;
        }

        boolean inVault = event.getClickedInventory() == gui.getInventory();
        if (inVault && gui.isNavSlot(event.getSlot())) {
            event.setCancelled(true);
            navigate(gui, player, event.getSlot());
            return;
        }

        if (!gui.canWithdraw() && inVault && !isDeposit(event.getAction())) {
            event.setCancelled(true);
            services.eraMessages.send(player, "vault-no-withdraw");
        }
    }

    private boolean isDeposit(InventoryAction action) {
        return action == InventoryAction.PLACE_ALL
                || action == InventoryAction.PLACE_ONE
                || action == InventoryAction.PLACE_SOME
                || action == InventoryAction.NOTHING;
    }

    private void navigate(VaultGui gui, Player player, int slot) {
        int pages = services.vaults.pages();
        int target;
        if (slot == gui.getContentSlots() && gui.getPage() > 0) {
            target = gui.getPage() - 1;
        } else if (slot == gui.getContentSlots() + 8 && gui.getPage() < pages - 1) {
            target = gui.getPage() + 1;
        } else {
            return;
        }
        Country country = gui.getCountry();
        gui.writeBack(services.vaults.get(country));
        services.vaults.save();
        // Opening an inventory from inside a click event has to wait a tick.
        services.plugin.getServer().getScheduler().runTask(services.plugin,
                () -> open(player, country, target));
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        VaultGui gui = guiOf(event.getView().getTopInventory());
        if (gui == null) {
            return;
        }
        for (int slot : event.getRawSlots()) {
            if (slot < gui.getInventory().getSize() && gui.isNavSlot(slot)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        VaultGui gui = guiOf(event.getInventory());
        if (gui == null) {
            return;
        }
        gui.writeBack(services.vaults.get(gui.getCountry()));
        services.vaults.save();
        services.eras.checkAuto(gui.getCountry());
    }

    public void open(Player player, Country country, int page) {
        VaultGui gui = new VaultGui(country, services.vaults.get(country), page,
                services.plugin.getConfig().getInt("vault.rows", 5),
                services.vaults.pages(),
                country.canWithdraw(player.getUniqueId()),
                services.eraMessages);
        gui.open(player);
    }
}
