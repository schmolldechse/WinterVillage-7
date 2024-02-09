package de.voldechse.wintervillage.masterbuilders.listener;

import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.gamestate.Types;
import de.voldechse.wintervillage.masterbuilders.teams.Team;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.ArrayList;
import java.util.List;

public class StructureGrowListener implements Listener {

    private final MasterBuilders plugin = InjectionLayer.ext().instance(MasterBuilders.class);

    @EventHandler
    public void execute(StructureGrowEvent event) {
        if (this.plugin.gameStateManager.currentGameState().getGameStatePhase() != Types.BUILDING_PHASE) {
            event.setCancelled(true);
            return;
        }

        if (event.getPlayer() != null && !event.getPlayer().hasMetadata("BUILD_MODE")) {
            Player player = event.getPlayer();
            if (this.plugin.gameManager.isSpectator(player)) {
                event.setCancelled(true);
                return;
            }

            if (!this.plugin.teamManager.isPlayerInTeam(player)) {
                event.setCancelled(true);
                return;
            }

            Team team = this.plugin.teamManager.getTeam(player);

            List<BlockState> blockStates = new ArrayList<>();
            for (BlockState blockState : event.getBlocks())
                if (this.plugin.teamManager.isBlockInPlot(team.teamId, blockState.getLocation()))
                    blockStates.add(blockState);

            event.getBlocks().clear();
            event.getBlocks().addAll(blockStates);
        }
    }
}