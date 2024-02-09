package de.voldechse.wintervillage.masterbuilders.listener;

import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItemListener implements Listener {

    private final MasterBuilders plugin = InjectionLayer.ext().instance(MasterBuilders.class);

    @EventHandler
    public void execute(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!this.plugin.hasMetadata(player, "BUILD_MODE"))
            event.setCancelled(true);
    }
}