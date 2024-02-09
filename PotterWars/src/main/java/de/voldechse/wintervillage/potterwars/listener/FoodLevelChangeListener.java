package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodLevelChangeListener implements Listener {

    @EventHandler
    public void execute(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();
        if (PotterWars.getInstance().gameManager.isSpectator(player)) {
            event.setCancelled(true);
            event.setFoodLevel(20);
            return;
        }

        Types gamePhase = PotterWars.getInstance().gameStateManager.currentGameState().getGameStatePhase();
        if (!(gamePhase == Types.INGAME || gamePhase == Types.OVERTIME)) {
            event.setCancelled(true);
            event.setFoodLevel(20);
        }
    }
}