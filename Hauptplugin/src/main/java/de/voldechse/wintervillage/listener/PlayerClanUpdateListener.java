package de.voldechse.wintervillage.listener;

import de.voldechse.wintervillage.WinterVillage;
import eu.cloudnetservice.driver.event.EventListener;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import me.joel.wv6.clansystem.event.PlayerClanUpdateEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerClanUpdateListener implements Listener {

    @EventHandler
    public void execute(PlayerClanUpdateEvent event) {
        Player player = event.getPlayer();

        InjectionLayer.ext().instance(WinterVillage.class).scoreboardManager.playerList();
    }
}