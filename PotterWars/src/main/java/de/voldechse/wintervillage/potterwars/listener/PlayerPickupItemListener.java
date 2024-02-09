package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class PlayerPickupItemListener implements Listener {

    @EventHandler
    public void execute(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        Types gamePhase = PotterWars.getInstance().gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase == Types.LOBBY || gamePhase == Types.RESTART) {
            event.setCancelled(true);
            return;
        }

        if (PotterWars.getInstance().gameManager.isSpectator(player)) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(false);
    }
}