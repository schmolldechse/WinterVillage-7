package de.voldechse.wintervillage.ttt.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class PlayerItemConsumeListener implements Listener {

    @EventHandler
    public void execute(PlayerItemConsumeEvent event) {
        event.setCancelled(true);
    }
}