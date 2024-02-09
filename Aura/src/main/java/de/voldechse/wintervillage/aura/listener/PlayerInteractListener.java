package de.voldechse.wintervillage.aura.listener;

import de.voldechse.wintervillage.aura.Aura;
import de.voldechse.wintervillage.aura.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {

    private final Aura plugin = InjectionLayer.ext().instance(Aura.class);

    @EventHandler
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();

        if (event.getClickedBlock() != null
                && (event.getClickedBlock().getType() == Material.CHEST || event.getClickedBlock().getType() == Material.TRAPPED_CHEST || event.getClickedBlock().getType() == Material.ENDER_CHEST || event.getClickedBlock().getType() == Material.FURNACE || event.getClickedBlock().getType() == Material.HOPPER || event.getClickedBlock().getType() == Material.ENCHANTING_TABLE || event.getClickedBlock().getType() == Material.DISPENSER || event.getClickedBlock().getType() == Material.DROPPER)) {
            event.setCancelled(true);
            return;
        }

        if (event.getItem() != null
                && event.getItem().hasItemMeta()
                && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§bTeleporter")) {
            if (!this.plugin.gameManager.isSpectator(player)) return;
            player.openInventory(this.plugin.gameManager.getSpectatorInventory());
            return;
        }

        if (event.getItem() != null
                && event.getItem().hasItemMeta()
                && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase("§cKompass")) {
            if (nearest(player) == null) {
                player.sendMessage(this.plugin.serverPrefix + "§cEs konnte kein Spieler gefunden werden");
                return;
            }

            Player nearest = nearest(player);

            PermissionUser nearestUser = this.plugin.permissionManagement.getOrCreateUserAsync(nearest.getUniqueId(), nearest.getName()).join();
            if (nearestUser == null) {
                player.sendMessage(plugin.serverPrefix + "§cAn error occurred while searching §c" + nearest.getName() + "§c's entry in database");
                return;
            }
            PermissionGroup nearestUsersGroup = plugin.permissionManagement.highestPermissionGroup(nearestUser);
            if (nearestUsersGroup == null) {
                player.sendMessage(plugin.serverPrefix + "§cAn error occurred while searching §c" + nearestUser.name() + "§c's entry in database");
                return;
            }

            player.setCompassTarget(nearest.getLocation());
            player.sendActionBar(Component.text("Kompass zeigt nun auf", NamedTextColor.YELLOW)
                    .append(Component.text(":", NamedTextColor.DARK_GRAY))
                    .append(Component.text(" " + nearestUsersGroup.color()))
                    .append(Component.text(nearestUser.name())));
        }
    }

    private Player nearest(Player player) {
        Player nearest = null;
        double maxDistance = Double.MAX_VALUE;

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getUniqueId().equals(player.getUniqueId())) continue;
            if (!online.getWorld().getName().equals(player.getWorld().getName())) continue;
            if (this.plugin.gameManager.isSpectator(online)) continue;
            double distance = player.getLocation().distance(online.getLocation());

            if (distance < maxDistance) {
                maxDistance = distance;
                nearest = online;
            }
        }

        return nearest;
    }
}