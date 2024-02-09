package de.voldechse.wintervillage.masterbuilders.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class EntitySpawnListener implements Listener {

    @EventHandler
    public void execute(EntitySpawnEvent event) {
        if (event.getEntityType().equals(EntityType.DROPPED_ITEM)) event.setCancelled(true);
    }
}