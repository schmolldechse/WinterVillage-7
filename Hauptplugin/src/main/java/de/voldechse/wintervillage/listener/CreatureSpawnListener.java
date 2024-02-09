package de.voldechse.wintervillage.listener;

import de.voldechse.wintervillage.WinterVillage;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CreatureSpawnListener implements Listener {

    @EventHandler
    public void execute(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();

        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.DISPENSE_EGG
                || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER)
            InjectionLayer.ext().instance(WinterVillage.class).setMetadata(entity, "FORBIDDEN", true);
    }
}