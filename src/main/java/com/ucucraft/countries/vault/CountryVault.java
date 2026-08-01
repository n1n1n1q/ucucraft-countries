package com.ucucraft.countries.vault;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class CountryVault {

    private final ItemStack[] contents;

    public CountryVault(int size) {
        this.contents = new ItemStack[size];
    }

    public int size() {
        return contents.length;
    }

    public ItemStack get(int index) {
        return contents[index];
    }

    public void set(int index, ItemStack stack) {
        contents[index] = stack == null || stack.getType().isAir() ? null : stack;
    }

    public int count(Material material) {
        int total = 0;
        for (ItemStack stack : contents) {
            if (stack != null && stack.getType() == material) {
                total += stack.getAmount();
            }
        }
        return total;
    }

    public Map<Material, Integer> summary() {
        Map<Material, Integer> map = new LinkedHashMap<>();
        for (ItemStack stack : contents) {
            if (stack != null) {
                map.merge(stack.getType(), stack.getAmount(), Integer::sum);
            }
        }
        return map;
    }

    /** Removes up to {@code amount} of a material and returns how many were actually removed. */
    public int remove(Material material, int amount) {
        int left = amount;
        for (int i = 0; i < contents.length && left > 0; i++) {
            ItemStack stack = contents[i];
            if (stack == null || stack.getType() != material) {
                continue;
            }
            int taken = Math.min(left, stack.getAmount());
            left -= taken;
            if (taken >= stack.getAmount()) {
                contents[i] = null;
            } else {
                stack.setAmount(stack.getAmount() - taken);
            }
        }
        return amount - left;
    }

    /** Adds what fits and returns the leftover amount. */
    public int add(ItemStack stack) {
        int left = stack.getAmount();
        int max = stack.getMaxStackSize();
        for (int i = 0; i < contents.length && left > 0; i++) {
            ItemStack slot = contents[i];
            if (slot != null && slot.isSimilar(stack) && slot.getAmount() < max) {
                int room = Math.min(max - slot.getAmount(), left);
                slot.setAmount(slot.getAmount() + room);
                left -= room;
            }
        }
        for (int i = 0; i < contents.length && left > 0; i++) {
            if (contents[i] == null) {
                ItemStack copy = stack.clone();
                int put = Math.min(max, left);
                copy.setAmount(put);
                contents[i] = copy;
                left -= put;
            }
        }
        return left;
    }

    public ItemStack[] page(int from, int length) {
        ItemStack[] out = new ItemStack[length];
        for (int i = 0; i < length; i++) {
            int index = from + i;
            out[i] = index < contents.length ? contents[index] : null;
        }
        return out;
    }

    public ItemStack[] raw() {
        return contents;
    }
}
