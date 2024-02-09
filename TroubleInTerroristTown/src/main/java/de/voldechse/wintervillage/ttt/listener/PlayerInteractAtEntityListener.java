package de.voldechse.wintervillage.ttt.listener;

import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

public class PlayerInteractAtEntityListener implements Listener {

    @EventHandler
    public void execute(PlayerInteractAtEntityEvent event) {
        if (!event.getPlayer().hasMetadata("BUILD_MODE")
                && event.getRightClicked() instanceof ItemFrame) event.setCancelled(true);
    }
}