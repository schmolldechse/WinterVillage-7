package de.voldechse.wintervillage.masterbuilders.listener;

import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.gamestate.Types;
import de.voldechse.wintervillage.masterbuilders.teams.Team;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

public class PlayerBucketListener implements Listener {

    private final MasterBuilders plugin = InjectionLayer.ext().instance(MasterBuilders.class);

    @EventHandler
    public void execute(PlayerBucketEmptyEvent event) {
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
        if (gamePhase != Types.BUILDING_PHASE) {
            event.setCancelled(true);
            return;
        }

        Team plot = this.plugin.teamManager.getTeam(player);
        if (!this.plugin.teamManager.isBlockInPlot(plot.teamId, event.getBlock().getLocation()))
            event.setCancelled(true);
    }

    @EventHandler
    public void execute(PlayerBucketFillEvent event) {
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
        if (gamePhase != Types.BUILDING_PHASE) {
            event.setCancelled(true);
            return;
        }

        Team plot = this.plugin.teamManager.getTeam(player);
        if (!this.plugin.teamManager.isBlockInPlot(plot.teamId, event.getBlock().getLocation()))
            event.setCancelled(true);
    }
}