package de.voldechse.wintervillage.listener;

import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.database.SpawnEggDatabase;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Random;

public class EntityDeathListener implements Listener {

    @EventHandler
    public void execute(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.getType() == EntityType.WITHER || entity.getType() == EntityType.ENDER_DRAGON) return;

        if (entity.getWorld().getName().equalsIgnoreCase("world")) return;

        if (entity.hasMetadata("FORBIDDEN")) return;

        SpawnEggDatabase database = InjectionLayer.ext().instance(WinterVillage.class).spawnEggDatabase;

        if (!database.isSaved(entity.getType())) return;

        double toSpawn = new Random().nextDouble(0, 100);
        if (toSpawn >= database.getProbability(entity.getType())) return;

        if (Material.getMaterial(entity.getType().name().toUpperCase() + "_SPAWN_EGG") == null) {
            InjectionLayer.ext().instance(WinterVillage.class).getInstance().getLogger().severe("Could not find spawn egg for " + entity.getType());
            return;
        }

        Material material = Material.getMaterial(entity.getType().name().toUpperCase() + "_SPAWN_EGG");
        if (event.getEntity() instanceof MushroomCow)
            material = Material.MOOSHROOM_SPAWN_EGG;

        entity.getWorld().dropItem(entity.getLocation().add(0, 1, 0),
                new ItemBuilder(material).build()
        );

        for (Entity nearby : entity.getNearbyEntities(4, 4, 4)) {
            if (!(nearby instanceof Player player)) continue;
            player.playSound(entity.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        }
    }
}
