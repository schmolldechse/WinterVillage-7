package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.potterwars.PotterWars;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangedWorldListener implements Listener {

    @EventHandler
    public void execute(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (!PotterWars.getInstance().gameManager.isSpectator(player)) return;

        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFlySpeed(0.2f);
    }
}