package com.ucucraft.countries.vault;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import com.ucucraft.countries.config.Messages;
import com.ucucraft.countries.model.Country;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class VaultGui implements InventoryHolder {

    private final Country country;
    private final int page;
    private final int contentSlots;
    private final boolean canWithdraw;
    private final Inventory inventory;

    public VaultGui(Country country, CountryVault vault, int page, int rows, int pages,
                    boolean canWithdraw, Messages lang) {
        this.country = country;
        this.page = page;
        this.contentSlots = rows * 9;
        this.canWithdraw = canWithdraw;

        Component title = MiniMessage.miniMessage().deserialize(lang.text("vault-title",
                "country", country.getName(),
                "page", String.valueOf(page + 1),
                "pages", String.valueOf(pages)));
        this.inventory = Bukkit.createInventory(this, contentSlots + 9, title);
        this.inventory.setContents(padded(vault.page(page * contentSlots, contentSlots)));
        buildNav(pages);
    }

    private ItemStack[] padded(ItemStack[] content) {
        ItemStack[] all = new ItemStack[contentSlots + 9];
        System.arraycopy(content, 0, all, 0, content.length);
        return all;
    }

    private void buildNav(int pages) {
        ItemStack filler = named(Material.GRAY_STAINED_GLASS_PANE, Component.empty());
        for (int i = contentSlots; i < contentSlots + 9; i++) {
            inventory.setItem(i, filler);
        }
        if (page > 0) {
            inventory.setItem(contentSlots, named(Material.ARROW,
                    Component.text("Previous page")));
        }
        if (page < pages - 1) {
            inventory.setItem(contentSlots + 8, named(Material.ARROW,
                    Component.text("Next page")));
        }
        inventory.setItem(contentSlots + 4, named(Material.PAPER,
                Component.text("Page " + (page + 1) + "/" + pages)));
    }

    private ItemStack named(Material material, Component name) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(name.decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
        stack.setItemMeta(meta);
        return stack;
    }

    public Country getCountry() {
        return country;
    }

    public int getPage() {
        return page;
    }

    public int getContentSlots() {
        return contentSlots;
    }

    public boolean canWithdraw() {
        return canWithdraw;
    }

    public boolean isNavSlot(int slot) {
        return slot >= contentSlots && slot < contentSlots + 9;
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }

    /** Writes the visible page back into the backing vault. */
    public void writeBack(CountryVault vault) {
        int from = page * contentSlots;
        for (int i = 0; i < contentSlots; i++) {
            vault.set(from + i, inventory.getItem(i));
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
