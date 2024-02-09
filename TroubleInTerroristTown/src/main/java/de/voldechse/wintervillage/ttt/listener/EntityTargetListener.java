package de.voldechse.wintervillage.ttt.listener;

import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.Role;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;

public class EntityTargetListener implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @EventHandler
    public void execute(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player) {
            if (this.plugin.gameManager.isSpectator(player)) {
                event.setTarget(null);
                return;
            }

            if (!this.plugin.roleManager.isPlayerAssigned(player)) {
                event.setTarget(null);
                return;
            }

            Role playerRole = this.plugin.roleManager.getRole(player);
            if (event.getEntity() instanceof Vex) {
                if (playerRole.roleId == 2) {
                    event.setTarget(null);
                    return;
                }

                event.setCancelled(false);
            } else if (event.getEntity() instanceof Creeper) {
                if (playerRole.roleId == 2) {
                    event.setTarget(null);
                    return;
                }

                event.setCancelled(false);
            }
        }
    }
}