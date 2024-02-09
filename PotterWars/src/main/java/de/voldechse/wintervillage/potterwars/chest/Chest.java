package de.voldechse.wintervillage.potterwars.chest;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class Chest {

    private static Random random = new Random();

    private Inventory inventory;
    private int minItems, maxItems;
    private List<ChestItem> items;

    public Chest(String inventoryName, List<ChestItem> items, int inventorySlots, int minItems, int maxItems) {
        this.inventory = Bukkit.createInventory(null, inventorySlots, inventoryName);
        this.minItems = minItems;
        this.maxItems = maxItems;
        this.items = items;

        this.createInventory();
    }

    private void createInventory() {
        int itemsInChest = Chest.random.nextInt(this.maxItems - this.minItems + 1) + this.minItems;
        for (int i = 0; i < itemsInChest; i++) {
            int inventorySlot = this.getFreeSlot(this.inventory);
            ChestItem chestItem = this.items.get(Chest.random.nextInt(this.items.size()));
            int itemCount = Chest.random.nextInt(chestItem.getMaxItem() - chestItem.getMinItem() + 1) + chestItem.getMinItem();

            ItemStack itemStack = chestItem.getItemStack();
            itemStack.setAmount(itemCount);
            inventory.setItem(inventorySlot, itemStack);
        }
    }

    private int getFreeSlot(Inventory inventory) {
        int inventorySlot = Chest.random.nextInt(this.inventory.getSize());
        if (inventory.getContents()[inventorySlot] == null) return inventorySlot;
        return this.getFreeSlot(inventory);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void clearInventory() {
        inventory.clear();
    }
}