package de.voldechse.wintervillage.ttt.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.TNTPrimeEvent;

public class TNTPrimeListener implements Listener {

    @EventHandler
    public void execute(TNTPrimeEvent event) {
        event.setCancelled(true);
    }
}