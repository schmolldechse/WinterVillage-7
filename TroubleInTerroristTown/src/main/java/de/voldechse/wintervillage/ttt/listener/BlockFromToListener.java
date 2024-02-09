package de.voldechse.wintervillage.ttt.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class BlockFromToListener implements Listener {

    @EventHandler
    public void execute(BlockFromToEvent event) {
        event.setCancelled(true);
    }
}