package de.voldechse.wintervillage.ttt.listener;

import de.voldechse.wintervillage.ttt.TTT;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangedWorldListener implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @EventHandler
    public void execute(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (!this.plugin.gameManager.isSpectator(player)) return;

        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFlySpeed(0.2f);
    }
}