package de.voldechse.wintervillage.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class PlayerResourcePackStatusListener implements Listener {

    @EventHandler
    public void execute(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();

        if (event.getStatus() == PlayerResourcePackStatusEvent.Status.DECLINED
                || event.getStatus() == PlayerResourcePackStatusEvent.Status.FAILED_DOWNLOAD) {
            player.kickPlayer("§cDu musst das Resoruce Pack annehmen, damit du den Server betreten kannst");
        }
    }
}