package de.voldechse.wintervillage.masterbuilders.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

public class BlockExplodeListener implements Listener {

    @EventHandler
    public void execute(BlockExplodeEvent event) {
        event.setCancelled(true);
    }
}