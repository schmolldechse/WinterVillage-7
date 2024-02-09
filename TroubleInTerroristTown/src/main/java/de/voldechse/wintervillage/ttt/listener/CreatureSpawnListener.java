package de.voldechse.wintervillage.ttt.listener;

import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CreatureSpawnListener implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @EventHandler
    public void execute(CreatureSpawnEvent event) {
        if (this.plugin.gameStateManager.currentGameState().getGameStatePhase() != Types.INGAME) {
            event.setCancelled(true);
            return;
        }
    }
}
