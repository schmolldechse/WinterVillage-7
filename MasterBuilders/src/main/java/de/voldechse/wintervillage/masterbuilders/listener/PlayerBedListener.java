package de.voldechse.wintervillage.masterbuilders.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

public class PlayerBedListener implements Listener {

    @EventHandler
    public void execute(PlayerBedEnterEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void execute(PlayerBedLeaveEvent event) {
        event.setCancelled(true);
    }
}