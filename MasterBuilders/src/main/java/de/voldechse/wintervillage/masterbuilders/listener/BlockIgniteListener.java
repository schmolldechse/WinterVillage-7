package de.voldechse.wintervillage.masterbuilders.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;

public class BlockIgniteListener implements Listener {

    @EventHandler
    public void execute(BlockIgniteEvent event) {
        event.setCancelled(true);
    }
}