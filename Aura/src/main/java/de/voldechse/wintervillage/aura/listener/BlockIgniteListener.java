package de.voldechse.wintervillage.aura.listener;

import de.voldechse.wintervillage.aura.Aura;
import de.voldechse.wintervillage.aura.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;

public class BlockIgniteListener implements Listener {

    private final Aura plugin = InjectionLayer.ext().instance(Aura.class);

    @EventHandler
    public void execute(BlockIgniteEvent event) {
        if (event.getPlayer() == null) return;

        if (event.getPlayer().hasMetadata("BUILD_MODE")) {
            event.setCancelled(false);
            return;
        }

        if (this.plugin.gameManager.isSpectator(event.getPlayer())) {
            event.setCancelled(true);
            return;
        }

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();

        if (gamePhase != Types.INGAME) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(false);
    }
}