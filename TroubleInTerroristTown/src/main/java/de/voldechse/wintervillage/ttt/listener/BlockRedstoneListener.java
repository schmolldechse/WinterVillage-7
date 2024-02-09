package de.voldechse.wintervillage.ttt.listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

public class BlockRedstoneListener implements Listener {

    @EventHandler
    public void execute(BlockRedstoneEvent event) {
        if (event.getBlock().getType() == Material.REDSTONE_LAMP)
            event.setNewCurrent(15);
    }
}