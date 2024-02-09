package de.voldechse.wintervillage.masterbuilders.listener;

import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.gamestate.Types;
import de.voldechse.wintervillage.masterbuilders.teams.Team;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;

public class HangingBreakByEntityListener implements Listener {

    private final MasterBuilders plugin = InjectionLayer.ext().instance(MasterBuilders.class);

    @EventHandler
    public void execute(HangingBreakByEntityEvent event) {
        if (event.getRemover() != null && !(event.getRemover() instanceof Player)) {
            event.setCancelled(true);
            return;
        }

        Player player = (Player) event.getRemover();

        if (player.hasMetadata("BUILD_MODE")) {
            event.setCancelled(false);
            return;
        }

        if (this.plugin.gameManager.isSpectator(player)) {
            event.setCancelled(true);
            return;
        }

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase != Types.BUILDING_PHASE) {
            event.setCancelled(true);
            return;
        }

        Team plot = this.plugin.teamManager.getTeam(player);
        if (!this.plugin.teamManager.isBlockInPlot(plot.teamId, event.getEntity().getLocation())) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(false);
    }
}