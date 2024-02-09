package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;

public class BlockBurnListener implements Listener {

    @EventHandler
    public void execute(BlockBurnEvent event) {
        Types gamePhase = PotterWars.getInstance().gameStateManager.currentGameState().getGameStatePhase();
        if (!(gamePhase == Types.INGAME || gamePhase == Types.OVERTIME))
            event.setCancelled(true);
    }
}