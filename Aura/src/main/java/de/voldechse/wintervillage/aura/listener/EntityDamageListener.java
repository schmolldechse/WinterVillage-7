package de.voldechse.wintervillage.aura.listener;

import de.voldechse.wintervillage.aura.Aura;
import de.voldechse.wintervillage.aura.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {

    private final Aura plugin = InjectionLayer.ext().instance(Aura.class);

    @EventHandler
    public void execute(EntityDamageEvent event) {
        if (!this.plugin.PVP_ENABLED) {
            event.setCancelled(true);
            return;
        }

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase != Types.INGAME) {
            event.setCancelled(true);
            return;
        }

        if (event.getEntity() instanceof Player receiver && this.plugin.gameManager.isSpectator(receiver)) {
            event.setCancelled(true);
            return;
        }
    }
}