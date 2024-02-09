package de.voldechse.wintervillage.aura.listener;

import de.voldechse.wintervillage.aura.Aura;
import de.voldechse.wintervillage.aura.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItemListener implements Listener {

    private final Aura plugin = InjectionLayer.ext().instance(Aura.class);

    @EventHandler
    public void execute(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata("BUILD_MODE")) {
            event.setCancelled(false);
            return;
        }

        if (this.plugin.gameManager.isSpectator(player)) {
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