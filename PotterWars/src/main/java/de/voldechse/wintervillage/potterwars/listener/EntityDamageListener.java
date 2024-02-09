package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {

    @EventHandler
    public void execute(EntityDamageEvent event) {
        if (!PotterWars.getInstance().PVP_ENABLED) {
            event.setCancelled(true);
            return;
        }

        Types gamePhase = PotterWars.getInstance().gameStateManager.currentGameState().getGameStatePhase();
        if (event.getEntity() instanceof Player player) {
            if (PotterWars.getInstance().gameManager.isSpectator(player)) {
                event.setCancelled(true);
                return;
            }

            if (!(gamePhase == Types.INGAME || gamePhase == Types.OVERTIME)) {
                event.setCancelled(true);
                return;
            }
        }
    }
}