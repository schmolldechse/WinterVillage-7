package de.voldechse.wintervillage.aura.listener;

import de.voldechse.wintervillage.aura.Aura;
import de.voldechse.wintervillage.aura.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;

public class BlockBurnListener implements Listener {

    private final Aura plugin = InjectionLayer.ext().instance(Aura.class);

    @EventHandler
    public void execute(BlockBurnEvent event) {
        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase != Types.INGAME)
            event.setCancelled(true);
    }
}