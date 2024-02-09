package de.voldechse.wintervillage.masterbuilders.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;

public class PotionSplashListener implements Listener {

    @EventHandler
    public void execute(PotionSplashEvent event) {
        event.setCancelled(true);
    }
}