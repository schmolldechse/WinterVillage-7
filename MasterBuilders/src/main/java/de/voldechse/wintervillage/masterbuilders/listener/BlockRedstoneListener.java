package de.voldechse.wintervillage.masterbuilders.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

public class BlockRedstoneListener implements Listener {

    @EventHandler
    public void execute(BlockRedstoneEvent event) {
        event.setNewCurrent(0);
    }
}