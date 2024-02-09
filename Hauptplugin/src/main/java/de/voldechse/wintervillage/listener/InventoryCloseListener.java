package de.voldechse.wintervillage.listener;

import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.database.ShopDatabase;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.UUID;

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void execute(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        WinterVillage plugin = InjectionLayer.ext().instance(WinterVillage.class);

        if (event.getInventory() != null && event.getView().getTitle().startsWith("§0Shop")) {
            String[] name = event.getView().getTitle().split("§0Shop ");

            UUID uuid = UUID.fromString(ChatColor.stripColor(name[1]));
            if (uuid == null) {
                event.getView().close();
                player.sendMessage(plugin.serverPrefix + "§cCould not determine shop identifier id");
                return;
            }

            ShopDatabase.PlayerShop playerShop = plugin.shopDatabase.shop(uuid);

            int newAmount = 0;

            for (ItemStack itemStack : event.getInventory().getContents())
                if (itemStack != null && itemStack.getType() == playerShop.material)
                    newAmount += itemStack.getAmount();

            plugin.shopDatabase.amount(uuid, BigDecimal.valueOf(newAmount));
            plugin.shopDatabase.editing(uuid, false);

            player.sendMessage(plugin.serverPrefix + "§aAktualisiert!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }
}
