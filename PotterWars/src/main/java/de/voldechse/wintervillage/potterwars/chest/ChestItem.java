package de.voldechse.wintervillage.potterwars.chest;

import org.bukkit.inventory.ItemStack;

public class ChestItem {

    private ItemStack itemStack;
    private int probility, min, max;

    public ChestItem(ItemStack itemStack, int probility, int min, int max) {
        this.itemStack = itemStack;
        this.probility = probility;
        this.min = min;
        this.max = max;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public int getItemProbility() {
        return this.probility;
    }

    public int getMinItem() {
        return this.min;
    }

    public int getMaxItem() {
        return this.max;
    }
}