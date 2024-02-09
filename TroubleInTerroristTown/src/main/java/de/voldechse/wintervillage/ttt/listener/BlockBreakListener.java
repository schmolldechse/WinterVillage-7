package de.voldechse.wintervillage.ttt.listener;

import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.roles.items.list.Item_Spinnennetzgranate;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @EventHandler
    public void execute(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata("SETUP_TESTER")
                && player.getItemInHand().getType() != Material.AIR
                && player.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase("§4SETUP")) {
            event.setCancelled(true);

            Location location = event.getBlock().getLocation();

            this.plugin.setMetadata(player, "EDITING", location);
            player.sendMessage(this.plugin.serverPrefix + "§cSchreibe §fnun entweder §eBARRIER_1 §8| §eBARRIER_2 §8| §eBARRIER_3 §8| §eLEFT_LAMP §8| §eRIGHT_LAMP §8| §eACTIVATION_BUTTON §foder §eTRAITOR_TRAP §fin den Chat, um die Position zu speichern oder §aDONE §fum den Bearbeitungsmodus zu verlassen");
            return;
        } else if (player.hasMetadata("EDIT_CHESTS")
                && (event.getBlock().getType() == Material.CHEST
                || event.getBlock().getType() == Material.ENDER_CHEST)) {
            event.setCancelled(true);

            if (this.plugin.positionManager.removeChestConfig(event.getBlock())) {
                player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                event.getBlock().setType(Material.AIR);
                event.setCancelled(false);
            } else {
                player.sendMessage(this.plugin.serverPrefix + "§cDiese Position ist nicht gespeichert");
                event.setCancelled(true);
                return;
            }
        } else if (player.hasMetadata("BUILD_MODE")) {
            event.setCancelled(false);
            return;
        }

        if (this.plugin.gameStateManager.currentGameState().getGameStatePhase() == Types.INGAME
                && Item_Spinnennetzgranate.PLACED_COBWEBS.contains(event.getBlock().getLocation())
                && event.getBlock().getType() == Material.COBWEB) {
            Item_Spinnennetzgranate.PLACED_COBWEBS.remove(event.getBlock().getLocation());
            event.setDropItems(false);
            event.setCancelled(false);
            return;
        }

        event.setCancelled(true);
    }
}