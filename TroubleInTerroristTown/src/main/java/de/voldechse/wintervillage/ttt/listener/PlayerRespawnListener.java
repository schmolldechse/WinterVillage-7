package de.voldechse.wintervillage.ttt.listener;

import de.voldechse.wintervillage.ttt.TTT;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @EventHandler
    public void execute(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        this.plugin.gameManager.setSpectator(player, true);
        this.plugin.CORPSES_MAP.forEach((entityId, corpse) -> corpse.respawnCorpse(player, entityId));
    }
}