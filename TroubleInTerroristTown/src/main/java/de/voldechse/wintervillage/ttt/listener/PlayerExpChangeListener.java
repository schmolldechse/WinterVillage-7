package de.voldechse.wintervillage.ttt.listener;

import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class PlayerExpChangeListener implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @EventHandler
    public void execute(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase == Types.PREPARING_START || gamePhase == Types.RESTART) {
            event.setAmount(0);
            return;
        }

        if (!(player.hasMetadata("BLASROHR")
                || player.hasMetadata("FLAMMENWERFER"))
                && gamePhase != Types.LOBBY) {
            event.setAmount(0);
            return;
        }
    }
}