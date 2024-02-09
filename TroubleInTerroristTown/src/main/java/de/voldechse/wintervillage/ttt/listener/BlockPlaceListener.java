package de.voldechse.wintervillage.ttt.listener;

import de.voldechse.wintervillage.ttt.TTT;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @EventHandler
    public void execute(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata("EDIT_CHESTS")
                && (event.getBlock().getType() == Material.CHEST
                || event.getBlock().getType() == Material.ENDER_CHEST)) {
            if (this.plugin.positionManager.addChestConfig(event.getBlock())) {
                player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                event.setCancelled(false);
            } else {
                player.sendMessage(this.plugin.serverPrefix + "§cDiese Position ist bereits gespeichert");
                event.setCancelled(true);
                return;
            }
        } else if (player.hasMetadata("BUILD_MODE")) {
            event.setCancelled(false);
            return;
        }

        event.setCancelled(true);
    }
}