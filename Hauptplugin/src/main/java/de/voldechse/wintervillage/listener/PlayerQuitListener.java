package de.voldechse.wintervillage.listener;

import de.voldechse.wintervillage.WinterVillage;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void execute(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        WinterVillage plugin = InjectionLayer.ext().instance(WinterVillage.class);

        plugin.scoreboardManager.updateScoreboard("currentOnline", " §a" + (Bukkit.getOnlinePlayers().size() - 1), "");

        if (!plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join()) return;
        PermissionUser permissionUser = plugin.permissionManagement.userAsync(player.getUniqueId()).join();
        if (permissionUser == null) return;

        PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) return;


        if (!player.hasMetadata("VANISH"))
            event.setQuitMessage("§c« " + permissionGroup.color() + permissionUser.name() + " §fhat Winter Village verlassen");
        else event.setQuitMessage(null);
    }
}