package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    @EventHandler
    public void execute(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Types gamePhase = PotterWars.getInstance().gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase == Types.PREPARING_START
                || (player.hasMetadata("PETRIFICUS_TOTALUS") && ((gamePhase == Types.INGAME) || (gamePhase == Types.OVERTIME)))) {
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
    }
}