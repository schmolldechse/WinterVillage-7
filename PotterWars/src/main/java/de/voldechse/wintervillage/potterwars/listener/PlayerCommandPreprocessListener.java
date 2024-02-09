package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.WinterVillage;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;

public class PlayerCommandPreprocessListener implements Listener {

    @EventHandler
    public void execute(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        WinterVillage plugin = InjectionLayer.ext().instance(WinterVillage.class);

        if (plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join() == null) {
            event.setCancelled(true);
            return;
        }
        PermissionUser permissionUser = plugin.permissionManagement.userAsync(player.getUniqueId()).join();
        if (permissionUser == null) {
            event.setCancelled(true);
            return;
        }

        PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) {
            event.setCancelled(true);
            return;
        }

        if (permissionGroup.potency() >= plugin.potencyDeveloper) return;

        disabled().forEach(command -> {
            if (event.getMessage().startsWith(command)) {
                event.setCancelled(true);
                player.sendMessage("§cIm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
            }
        });
    }

    private List<String> disabled() {
        return List.of( "/?", "/about", "/help", "/pl", "/plugins", "/reload", "/rl", "/timings", "/ver", "/versions",
                "/bukkit:?", "/bukkit:about", "/bukkit:help", "/bukkit:pl", "/bukkit:plugins", "/bukkit:reload", "/bukkit:rl", "/bukkit:timings",
                "/icanhasbukkit",
                "/me", "/say");
    }
}
