package de.voldechse.wintervillage.masterbuilders.listener;

import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class PlayerMoveListener implements Listener {

    private final MasterBuilders plugin = InjectionLayer.ext().instance(MasterBuilders.class);

    @EventHandler
    public void execute(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase == Types.VOTING_THEME) {
            Location from = event.getFrom();
            Location to = event.getTo();
            double x = Math.floor(from.getX());
            double z = Math.floor(from.getZ());
            if (Math.floor(to.getX()) != x || Math.floor(to.getZ()) != z) {
                x += .5;
                z += .5;
                player.teleport(new Location(from.getWorld(), x, from.getY(), z, from.getYaw(), from.getPitch()));
                player.teleport(player);
            }
        }

        if (gamePhase == Types.BUILDING_PHASE
                && this.plugin.teamManager.isPlayerInTeam(player)
                && player.getLocation().distance(this.plugin.teamManager.getTeam(player).getPlayerSpawn_34ezt()) > this.plugin.teleportationTreshold
                && !player.hasMetadata("BUILD_MODE")) {
            Vector playersVector = player.getLocation().toVector();
            Vector spawnVector = this.plugin.teamManager.getTeam(player).getPlayerSpawn_34ezt().toVector();

            double y = player.getLocation().getY() > spawnVector.getY() ? -.5 : .5;

            Vector vector = spawnVector.clone().subtract(playersVector).multiply(2.5 / spawnVector.distance(playersVector)).setY(y);

            player.setVelocity(vector);
            player.sendMessage(this.plugin.serverPrefix + "§cDu darfst dein Grundstück nicht verlassen!");
        }
    }
}