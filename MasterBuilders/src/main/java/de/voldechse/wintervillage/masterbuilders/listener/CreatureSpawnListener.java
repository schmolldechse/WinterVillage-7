package de.voldechse.wintervillage.masterbuilders.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CreatureSpawnListener implements Listener {

    @EventHandler
    public void execute(CreatureSpawnEvent event) {
        if (!(event.getEntity().getType() == EntityType.VILLAGER
                || event.getEntity().getType() == EntityType.ARMOR_STAND)) event.setCancelled(true);
    }
}