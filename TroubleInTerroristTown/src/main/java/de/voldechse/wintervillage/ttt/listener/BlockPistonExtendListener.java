package de.voldechse.wintervillage.ttt.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;

public class BlockPistonExtendListener implements Listener {

    @EventHandler
    public void execute(BlockPistonExtendEvent event) {
        event.setCancelled(true);
    }
}