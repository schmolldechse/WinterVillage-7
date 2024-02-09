package de.voldechse.wintervillage.masterbuilders.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;

public class LeavesDecayListener implements Listener {

    @EventHandler
    public void execute(LeavesDecayEvent event) {
        event.setCancelled(true);
    }
}