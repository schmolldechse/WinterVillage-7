package de.voldechse.wintervillage.listener;

import de.voldechse.wintervillage.WinterVillage;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangedWorldListener implements Listener {

    @EventHandler
    public void execute(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        WinterVillage plugin = InjectionLayer.ext().instance(WinterVillage.class);

        if (!plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join()) {
            if (player.getAllowFlight()) player.setAllowFlight(false);
            if (player.isFlying()) player.setFlying(false);
            return;
        }

        PermissionUser permissionUser = plugin.permissionManagement.userAsync(player.getUniqueId()).join();
        if (permissionUser == null) {
            if (player.getAllowFlight()) player.setAllowFlight(false);
            if (player.isFlying()) player.setFlying(false);
            return;
        }
        PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) {
            if (player.getAllowFlight()) player.setAllowFlight(false);
            if (player.isFlying()) player.setFlying(false);
            return;
        }

        if (permissionGroup.potency() == plugin.potencySpectator) return;

        if (permissionGroup.potency() < plugin.potencySupporter && player.hasMetadata("FLYING")) {
            if (player.getAllowFlight()) player.setAllowFlight(false);
            if (player.isFlying()) player.setFlying(false);
            plugin.removeMetadata(player, "FLYING");
            player.sendMessage(plugin.serverPrefix + "§eDu kannst nun nicht mehr fliegen");
            return;
        }
    }
}
