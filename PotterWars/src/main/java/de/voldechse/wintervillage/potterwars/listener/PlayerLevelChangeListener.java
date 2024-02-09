package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.potterwars.PotterWars;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;

public class PlayerLevelChangeListener implements Listener {

    @EventHandler
    public void execute(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();
        if (PotterWars.getInstance().gameManager.isSpectator(player)) {
            player.setLevel(0);
            player.setExp(0.0F);
            return;
        }
    }
}