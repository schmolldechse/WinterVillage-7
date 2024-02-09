package de.voldechse.wintervillage.listener;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class EntityExplodeListener implements Listener {

    @EventHandler
    public void execute(EntityExplodeEvent event) {
        if (event.getLocation().getWorld().getName().equals("world")
                && event.getEntityType() == EntityType.PRIMED_TNT)
            event.blockList().clear();
    }
}