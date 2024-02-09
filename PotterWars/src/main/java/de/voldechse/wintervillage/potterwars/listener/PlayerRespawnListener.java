package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.potterwars.PotterWars;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {

    @EventHandler
    public void execute(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        PotterWars.getInstance().scoreboardManager.generateScoreboard(player);
        PotterWars.getInstance().gameManager.setSpectator(player, true);
    }
}