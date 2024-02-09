package de.voldechse.wintervillage.potterwars.kit;

import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.kit.list.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.*;

public class KitManager {

    public List<Kit> kitList = new ArrayList<Kit>();
    private Map<Player, Kit> playerKit = new HashMap<Player, Kit>();

    public KitManager() {
        this.kitList.add(new Kit_Rüstungsträger());
        this.kitList.add(new Kit_Bellatrix());
        this.kitList.add(new Kit_Bauarbeiter());
        this.kitList.add(new Kit_Dumbledore());
        this.kitList.add(new Kit_Hermine());
        this.kitList.add(new Kit_MadamPomfrey());
        this.kitList.add(new Kit_Neville());
        this.kitList.add(new Kit_Snape());
        this.kitList.add(new Kit_Voldemort());
        this.kitList.add(new Kit_Bogenschütze());
    }

    public void setKit(Player player, Kit kit) {
        this.removeKit(player);
        this.playerKit.put(player, kit);
    }

    public void removeKit(Player player) {
        if (!hasKit(player))
            return;
        this.playerKit.remove(player);
    }

    public boolean hasKit(Player player) {
        if (this.playerKit.containsKey(player))
            return true;
        return false;
    }

    public void openInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 18, "§cKit auswählen");
        this.kitList.forEach(kit -> {
            if (kit.isUseable()) inventory.addItem(kit.getIcon());
        });
        player.openInventory(inventory);
    }

    public void giveItems(Player player) {
        if (!this.hasKit(player)) {
            Kit kit = this.kitList.get(new Random().nextInt(this.kitList.size()));
            this.setKit(player, kit);
            player.sendMessage(PotterWars.getInstance().serverPrefix + "§eDa du dir kein Kit ausgewählt hast, wurde dir das Kit §c" + kit.getName() + " §eausgewählt");
        }
        this.playerKit.get(player).loadItems(player);
    }

    public Kit getById(int id) {
        for (Kit kits : this.kitList) {
            if (kits.getId() != id) continue;
            return kits;
        }
        return null;
    }

    public Kit getKit(Player player) {
        if (this.playerKit.containsKey(player))
            return this.playerKit.get(player);
        return null;
    }

    public String getKitByName(Player player) {
        if (this.playerKit.containsKey(player))
            return this.playerKit.get(player).getName();
        return "§cKeins ausgewählt";
    }
}