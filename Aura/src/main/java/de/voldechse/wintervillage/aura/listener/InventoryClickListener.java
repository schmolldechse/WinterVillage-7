package de.voldechse.wintervillage.aura.listener;

import de.voldechse.wintervillage.aura.Aura;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {

    private final Aura plugin = InjectionLayer.ext().instance(Aura.class);

    private final Component error = Component.text(this.plugin.serverPrefix)
            .append(Component.text("Something seems to be not working. Try again", NamedTextColor.RED));

    @EventHandler
    public void execute(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR)
            return;

        ItemStack currentItem = event.getCurrentItem();
        if (event.getView().getTitle().equalsIgnoreCase("§bTeleporter")) {
            event.setCancelled(true);

            String displayName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());

            Player toSpectate = Bukkit.getPlayer(displayName);
            if (toSpectate == null) {
                player.sendMessage(error);
                return;
            }

            if (this.plugin.gameManager.isSpectator(toSpectate)) {
                player.sendMessage(error);
                return;
            }

            if (!plugin.permissionManagement.containsUserAsync(toSpectate.getUniqueId()).join()) {
                event.setCancelled(true);
                player.sendMessage(plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
                return;
            }

            PermissionUser permissionUser = plugin.permissionManagement.userAsync(toSpectate.getUniqueId()).join();
            if (permissionUser == null) {
                event.setCancelled(true);
                player.sendMessage(plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
                return;
            }
            PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
            if (permissionGroup == null) {
                event.setCancelled(true);
                player.sendMessage(plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
                return;
            }

            player.teleport(toSpectate);
            player.sendMessage(this.plugin.serverPrefix + "§7Du wurdest zu " + permissionGroup.color() + permissionUser.name() + " §7teleportiert");
            return;
        }
    }
}