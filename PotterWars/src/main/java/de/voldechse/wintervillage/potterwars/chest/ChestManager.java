package de.voldechse.wintervillage.potterwars.chest;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChestManager {

    private final String name;
    private final Map<Location, Chest> chests;
    private final List<ChestItem> items;
    private final int minItems, maxItems, slots;

    private final Map<Player, List<Location>> chestsOpened;

    public ChestManager(String name, List<ChestItem> items, int maxItems, int minItems, int slots) {
        this.name = name;
        this.maxItems = maxItems;
        this.minItems = minItems;
        this.chests = new HashMap<>();

        if (slots % 9 == 0) this.slots = slots;
        else this.slots = 27;

        this.items = new ArrayList<>();
        for (ChestItem chestItem : items) {
            for (int j = 0; j < chestItem.getItemProbility(); j++) this.items.add(chestItem);
        }
        this.chestsOpened = new HashMap<>();
    }

    public Chest getAt(Location location) {
        if (this.chests.containsKey(location)) return this.chests.get(location);
        return this.getNewChest(location);
    }

    private Chest getNewChest(Location location) {
        Chest chest = new Chest(this.name, this.items, this.slots, this.minItems, this.maxItems);
        this.chests.put(location, chest);
        return chest;
    }

    public void refillChest() {
        this.chests.clear();
    }

    public void addChest(Player player, Location location) {
        if (this.chestsOpened.containsKey(player)) {
            if (!this.chestsOpened.get(player).contains(location)) {
                this.chestsOpened.get(player).add(location);
            }
        } else {
            this.chestsOpened.put(player, new ArrayList<>());
            this.addChest(player, location);
        }
    }

    public int getChestOpened(Player player) {
        if (this.chestsOpened.containsKey(player))
            return this.chestsOpened.get(player).size();
        return 0;
    }

    public String getName() {
        return this.name;
    }
}