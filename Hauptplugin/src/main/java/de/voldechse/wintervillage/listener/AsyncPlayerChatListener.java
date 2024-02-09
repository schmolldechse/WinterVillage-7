package de.voldechse.wintervillage.listener;

import de.voldechse.wintervillage.WinterVillage;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.concurrent.ExecutionException;

public class AsyncPlayerChatListener implements Listener {

    @EventHandler
    public void execute(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        WinterVillage plugin = InjectionLayer.ext().instance(WinterVillage.class);

        if (!plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join()) {
            event.setCancelled(true);
            player.sendMessage(plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
            return;
        }

        PermissionUser permissionUser = plugin.permissionManagement.userAsync(player.getUniqueId()).join();
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

        if (plugin.CHAT_MUTED && permissionGroup.potency() < plugin.potencySupporter) {
            event.setCancelled(true);
            player.sendMessage(plugin.serverPrefix + "§cDer Chat ist deaktiviert");
            return;
        }

        if (player.hasMetadata("MUTE")) {
            event.setCancelled(true);
            player.sendMessage(plugin.serverPrefix + "§cDer Chat ist für dich deaktiviert");
            return;
        }

        event.setMessage(event.getMessage().replace("\uD83D\uDD61", ""));

        /**
        event.setFormat((player.hasMetadata("PLAYERS_HEAD") ? player.getMetadata("PLAYERS_HEAD").get(0).asString() + " §r" : "§r")
                + permissionGroup.color().replace("&", "§") + permissionGroup.name() + " §f" + permissionUser.name() + " §8| §f%2$s");
         */

        //event.setFormat(" " + permissionGroup.color() + permissionGroup.name() + " §f" + player.getName() + " §8| §f" + event.getMessage());
        event.setFormat(" " + permissionGroup.color() + permissionGroup.name() + " §f%1$s" + " §8| §f%2$s");
    }
}
