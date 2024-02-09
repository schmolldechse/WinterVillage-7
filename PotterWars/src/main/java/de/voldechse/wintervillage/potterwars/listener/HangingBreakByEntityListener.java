package de.voldechse.wintervillage.potterwars.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;

public class HangingBreakByEntityListener implements Listener {

    @EventHandler
    public void execute(HangingBreakByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            event.setCancelled(true);
            return;
        }

        Player player = (Player) event.getEntity();
        if (player.hasMetadata("BUILD_MODE")) {
            event.setCancelled(false);
            return;
        }

        event.setCancelled(true);
    }
}