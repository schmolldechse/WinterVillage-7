package de.voldechse.wintervillage.ttt.game.corpse;

import com.mojang.datafixers.optics.Inj1;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.ttt.TTT;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

import java.util.UUID;

public class Hologram {
    
    private final TTT plugin;

    private final String[] lines;
    private final Document document;

    public Hologram(Document document, String... lines) {
        this.plugin = InjectionLayer.ext().instance(TTT.class);
        
        this.document = document;
        this.lines = lines;
    }

    public void spawn(Location location, long millis) {
        for (String line : lines) {
            ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class);

            armorStand.setGravity(false);
            armorStand.setVisible(false);
            armorStand.setInvulnerable(true);

            armorStand.setCustomNameVisible(true);
            armorStand.setCustomName(line);

            location.subtract(0, 0.4D, 0);

            this.plugin.HOLOGRAM_ARMORSTANDS.add(armorStand);

            if (armorStand.getCustomName().contains("§7Tot seit")) {
                UUID uuid = UUID.fromString(document.getString("diedPlayer_UUID"));
                this.plugin.IDENTIFICATION_MAP_TIMER.put(uuid, armorStand);
                this.plugin.setMetadata(armorStand, "IDENTIFICATION_TIMER", millis);
            }

            if (armorStand.getCustomName().contains("§7Getötet von")) {
                UUID uuid = UUID.fromString(document.contains("killer_UUID") ? document.getString("killer_UUID") : document.getString("diedPlayer_UUID"));
                this.plugin.IDENTIFICATION_MAP_KILLED_BY.put(uuid, armorStand);
                this.plugin.setMetadata(armorStand, "IDENTIFICATION_KILLED_BY", uuid);
            }
        }
    }
}