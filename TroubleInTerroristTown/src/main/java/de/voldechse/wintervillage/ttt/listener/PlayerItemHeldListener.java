package de.voldechse.wintervillage.ttt.listener;

import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;

public class PlayerItemHeldListener implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @EventHandler
    public void execute(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata("BLASROHR")
                && player.getInventory().getItem(event.getNewSlot()) != null
                && player.getInventory().getItem(event.getNewSlot()).getItemMeta() != null
                && player.getInventory().getItem(event.getNewSlot()).getItemMeta().getDisplayName().equalsIgnoreCase("§2Blasrohr")) {
            int shotsLeft = player.getMetadata("BLASROHR").get(0).asInt();

            player.setLevel(shotsLeft);
            player.setExp((float) shotsLeft / 5);
        } else if (player.hasMetadata("FLAMMENWERFER")
                && player.getInventory().getItem(event.getNewSlot()) != null
                && player.getInventory().getItem(event.getNewSlot()).getItemMeta() != null
                //&& player.getInventory().getItem(event.getNewSlot()).getItemMeta().getDisplayName().equalsIgnoreCase("§6Flammenwerfer")) {
                && player.getInventory().getItem(event.getNewSlot()).getItemMeta().getDisplayName().equalsIgnoreCase("§6Krampus Feuerrute")) {
            int shotsLeft = player.getMetadata("FLAMMENWERFER").get(0).asInt();

            player.setLevel(shotsLeft);
            player.setExp((float) shotsLeft / 10);
        } else if (this.plugin.gameStateManager.currentGameState().getGameStatePhase() != Types.LOBBY) {
            player.setLevel(0);
            player.setExp(0);
        }
    }
}