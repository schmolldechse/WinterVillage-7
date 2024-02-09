package de.voldechse.wintervillage.ttt.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.PortalCreateEvent;

public class PortalCreateListener implements Listener {

    @EventHandler
    public void execute(PortalCreateEvent event) {
        event.setCancelled(true);
    }
}