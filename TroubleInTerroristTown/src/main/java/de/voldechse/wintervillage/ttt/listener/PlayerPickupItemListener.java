package de.voldechse.wintervillage.ttt.listener;

import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class PlayerPickupItemListener implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @EventHandler
    public void execute(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata("BUILD_MODE")) {
            event.setCancelled(false);
            return;
        }

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        if (!(gamePhase.equals(Types.INGAME) || gamePhase.equals(Types.PREPARING_START))) {
            event.setCancelled(true);
            return;
        }

        if (this.plugin.gameManager.isSpectator(player)) {
            event.setCancelled(true);
            return;
        }

        if (this.plugin.roleManager.getRole(player) != null
                && this.plugin.roleManager.getRole(player).roleId == 2
                && event.getItem().getItemStack().getType() == Material.TRIDENT) {
            event.setCancelled(false);
            return;
        }

        if (this.plugin.ALLOWED_ITEMS_TO_DROP.contains(event.getItem().getItemStack().getType())) {
            event.setCancelled(false);
            return;
        }

        event.setCancelled(true);
    }
}