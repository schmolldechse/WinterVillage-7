package de.voldechse.wintervillage.aura.listener;

import de.voldechse.wintervillage.aura.Aura;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class EntityTargetListener implements Listener {

    private final Aura plugin = InjectionLayer.ext().instance(Aura.class);

    @EventHandler
    public void execute(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player) {
            if (this.plugin.gameManager.isSpectator(player)) {
                event.setTarget(null);
                return;
            }


        }
    }
}
