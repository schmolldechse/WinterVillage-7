package de.voldechse.wintervillage.ttt.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class PlayerBucketListener implements Listener {

    @EventHandler
    public void execute(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (!player.hasMetadata("BUILD_MODE")) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(false);
    }

    @EventHandler
    public void execute(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        if (!player.hasMetadata("BUILD_MODE")) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(false);
    }
}