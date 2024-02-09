package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.ArrayList;
import java.util.List;

public class BlockPlaceListener implements Listener {

    //TODO: Delete this list later
    public static List<Location> blockedChestLocation = new ArrayList<Location>();

    @EventHandler
    public void execute(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata("BUILD_MODE")) {
            event.setCancelled(false);
            return;
        }

        if (PotterWars.getInstance().gameManager.isSpectator(player)) {
            event.setCancelled(true);
            return;
        }

        Types gamePhase = PotterWars.getInstance().gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase == Types.LOBBY || gamePhase == Types.RESTART || gamePhase == Types.PREPARING_START) {
            event.setCancelled(true);
            return;
        }

        if (event.getBlockPlaced().getType() == Material.CHEST) {
            if (!blockedChestLocation.contains(event.getBlockPlaced().getLocation()))
                blockedChestLocation.add(event.getBlockPlaced().getLocation());
        }

        event.setCancelled(false);
    }
}