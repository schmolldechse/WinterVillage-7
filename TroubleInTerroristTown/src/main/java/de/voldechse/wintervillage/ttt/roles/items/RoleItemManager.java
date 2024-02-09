package de.voldechse.wintervillage.ttt.roles.items;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.items.list.*;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RoleItemManager {

    private final TTT plugin;

    private final List<RoleItem> INNOCENT_ITEMS, DETECTIVE_ITEMS, TRAITOR_ITEMS;

    public Map<UUID, Map<RoleItem, Integer>> ALREADY_BOUGHT;

    public RoleItemManager() {
        this.plugin = InjectionLayer.ext().instance(TTT.class);
        
        this.INNOCENT_ITEMS = new ArrayList<>();
        this.DETECTIVE_ITEMS = new ArrayList<>();
        this.TRAITOR_ITEMS = new ArrayList<>();
        this.ALREADY_BOUGHT = new HashMap<>();

        this.INNOCENT_ITEMS.add(new Item_Flammenwerfer());
        this.INNOCENT_ITEMS.add(new Item_ZweiteChance());

        this.DETECTIVE_ITEMS.add(new Item_Flammenwerfer());
        this.DETECTIVE_ITEMS.add(new Item_Defuser());
        this.DETECTIVE_ITEMS.add(new Item_Fallschirm());
        this.DETECTIVE_ITEMS.add(new Item_Handschellen());
        this.DETECTIVE_ITEMS.add(new Item_Heilstation());
        //SUPER-IDENTIFIZIERER
        this.DETECTIVE_ITEMS.add(new Item_RandomTester());
        this.DETECTIVE_ITEMS.add(new Item_Spinnennetzgranate());
        this.DETECTIVE_ITEMS.add(new Item_Blendgranate());
        //TODO: this.DETECTIVE_ITEMS.add(new Item_TraitorDetektor());
        this.DETECTIVE_ITEMS.add(new Item_Schildkrötenhelm());
        this.DETECTIVE_ITEMS.add(new Item_Radar());
        this.DETECTIVE_ITEMS.add(new Item_OneShotBogen());
        this.DETECTIVE_ITEMS.add(new Item_Enteiser());
        //SCHILD
        //RAUCHGRANATE
        this.DETECTIVE_ITEMS.add(new Item_ZweiteChance());

        this.TRAITOR_ITEMS.add(new Item_Blasrohr());
        this.TRAITOR_ITEMS.add(new Item_C4());
        this.TRAITOR_ITEMS.add(new Item_Flammenwerfer());
        this.TRAITOR_ITEMS.add(new Item_Lagerfeuer());
        this.TRAITOR_ITEMS.add(new Item_Plagegeister());
        this.TRAITOR_ITEMS.add(new Item_InnocentTicket());
        this.TRAITOR_ITEMS.add(new Item_BoomBody());
        this.TRAITOR_ITEMS.add(new Item_Kannibalismus());
        this.TRAITOR_ITEMS.add(new Item_FallenTicket());
        this.TRAITOR_ITEMS.add(new Item_Schneemann());
        //Tarnkappe
        this.TRAITOR_ITEMS.add(new Item_CreeperPfeile());
        this.TRAITOR_ITEMS.add(new Item_Bumerang());
        this.TRAITOR_ITEMS.add(new Item_Radar());
        //MINIGUN
        this.TRAITOR_ITEMS.add(new Item_Landmine());
        this.TRAITOR_ITEMS.add(new Item_Blitzstab());
        this.TRAITOR_ITEMS.add(new Item_Blendgranate());
        this.TRAITOR_ITEMS.add(new Item_StomperBoots());
        this.TRAITOR_ITEMS.add(new Item_TeleportGun());
        this.TRAITOR_ITEMS.add(new Item_ZweiteChance());

        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_Radar(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_ZweiteChance(), this.plugin.getInstance());

        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_Defuser(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_Fallschirm(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_Handschellen(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_Heilstation(), this.plugin.getInstance());
        //SUPER-IDENTIFIZIERER
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_RandomTester(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_Spinnennetzgranate(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_Blendgranate(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_TraitorDetektor(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_Schildkrötenhelm(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_OneShotBogen(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_Enteiser(), this.plugin.getInstance());
        //SCHILD
        //RAUCHGRANATE

        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_Blasrohr(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_C4(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_Flammenwerfer(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_Lagerfeuer(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_Plagegeister(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_InnocentTicket(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_BoomBody(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_Kannibalismus(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_Schneemann(), this.plugin.getInstance());
        //Tarnkappe
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_CreeperPfeile(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_Bumerang(), this.plugin.getInstance());
        //MINIGUN
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_Landmine(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_Blitzstab(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_StomperBoots(), this.plugin.getInstance());
        this.plugin.getInstance().getServer().getPluginManager().registerEvents(new Item_TeleportGun(), this.plugin.getInstance());
    }

    public void openShop_innocent(Player player) {
        int inventorySize = 18 + (this.INNOCENT_ITEMS.size() / 9 + 1) * 9;

        //Inventory inventory = Bukkit.createInventory(null, inventorySize, "§8Detective-Shop");
        Inventory inventory = Bukkit.createInventory(null, inventorySize, "§8Elfen-Shop");
        for (int i = 0; i < 9; i++) inventory.setItem(i, new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE).build());

        this.INNOCENT_ITEMS.forEach(item -> {
            inventory.addItem(item.getIcon(false));
        });

        int[] empty = getLastEmptySlots(inventory, 9);
        for (int i : empty) inventory.setItem(i, new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE).build());

        player.openInventory(inventory);
    }

    public void openShop_detective(Player player) {
        int inventorySize = 18 + (this.DETECTIVE_ITEMS.size() / 9 + 1) * 9;

        //Inventory inventory = Bukkit.createInventory(null, inventorySize, "§8Detective-Shop");
        Inventory inventory = Bukkit.createInventory(null, inventorySize, "§8Mr. Frost-Shop");
        for (int i = 0; i < 9; i++) inventory.setItem(i, new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE).build());

        this.DETECTIVE_ITEMS.forEach(item -> {
            inventory.addItem(item.getIcon(false));
        });

        int[] empty = getLastEmptySlots(inventory, 9);
        for (int i : empty) inventory.setItem(i, new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE).build());

        player.openInventory(inventory);
    }

    public void openShop_traitor(Player player) {
        int inventorySize = 18 + (this.TRAITOR_ITEMS.size() / 9 + 1) * 9;

        //Inventory inventory = Bukkit.createInventory(null, inventorySize, "§8Traitor-Shop");
        Inventory inventory = Bukkit.createInventory(null, inventorySize, "§8Krampus-Shop");
        for (int i = 0; i < 9; i++) inventory.setItem(i, new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE).build());

        this.TRAITOR_ITEMS.forEach(item -> {
            inventory.addItem(item.getIcon(true));
        });

        int[] empty = getLastEmptySlots(inventory, 9);
        for (int i : empty) inventory.setItem(i, new ItemBuilder(Material.CYAN_STAINED_GLASS_PANE).build());

        player.openInventory(inventory);
    }

    public RoleItem getItemByName(String itemName) {
        RoleItem item = null;
        for (RoleItem x : DETECTIVE_ITEMS)
            if (x.getName().equals(itemName))
                item = x;
        for (RoleItem x : TRAITOR_ITEMS)
            if (x.getName().equals(itemName))
                item = x;
        return item;
    }

    public int purchaseCount(Player player, RoleItem roleItem) {
        if (ALREADY_BOUGHT.containsKey(player.getUniqueId())) {
            Map<RoleItem, Integer> buyData = ALREADY_BOUGHT.get(player.getUniqueId());
            if (buyData.containsKey(roleItem)) return buyData.get(roleItem);
        }
        return 0;
    }

    public void increasePurchaseCount(Player player, RoleItem roleItem) {
        if (!ALREADY_BOUGHT.containsKey(player.getUniqueId())) {
            Map<RoleItem, Integer> buyData = new HashMap<>();
            buyData.put(roleItem, 1);
            ALREADY_BOUGHT.put(player.getUniqueId(), buyData);
            return;
        }

        Map<RoleItem, Integer> buyData = ALREADY_BOUGHT.get(player.getUniqueId());
        if (!buyData.containsKey(roleItem)) {
            buyData.put(roleItem, 1);
            return;
        }

        int currentCount = buyData.get(roleItem);
        buyData.put(roleItem, currentCount + 1);
    }

    private int[] getLastEmptySlots(Inventory inventory, int amount) {
        int[] empty = new int[amount];
        int count = 0;

        for (int i = inventory.getSize() - 1; i >= 0 && count < 9; i--) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack == null) {
                empty[count] = i;
                count++;
            }
        }

        return empty;
    }
}