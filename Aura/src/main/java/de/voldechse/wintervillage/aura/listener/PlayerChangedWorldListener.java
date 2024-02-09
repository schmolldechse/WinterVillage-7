package de.voldechse.wintervillage.aura.listener;

import de.voldechse.wintervillage.aura.Aura;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class PlayerChangedWorldListener implements Listener {

    private final Aura plugin = InjectionLayer.ext().instance(Aura.class);

    @EventHandler
    public void execute(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (!this.plugin.gameManager.isSpectator(player)) return;

        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFlySpeed(0.2f);
    }
}