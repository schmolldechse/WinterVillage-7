package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;

public class BlockIgniteListener implements Listener {

    @EventHandler
    public void execute(BlockIgniteEvent event) {
        if (event.getPlayer() == null) return;

        if (PotterWars.getInstance().gameManager.isSpectator(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }

        Types gamePhase = PotterWars.getInstance().gameStateManager.currentGameState().getGameStatePhase();
        if (!(gamePhase == Types.INGAME || gamePhase == Types.OVERTIME)) event.setCancelled(true);
        if (event.getCause() == BlockIgniteEvent.IgniteCause.FLINT_AND_STEEL) {
            event.setCancelled(false);
            return;
        }
        event.setCancelled(true);
    }
}