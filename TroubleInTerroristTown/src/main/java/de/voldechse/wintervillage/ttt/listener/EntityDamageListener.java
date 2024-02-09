package de.voldechse.wintervillage.ttt.listener;

import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @EventHandler
    public void execute(EntityDamageEvent event) {
        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        if (event.getEntity() instanceof Player && gamePhase != Types.INGAME) {
            event.setCancelled(true);
            return;
        }

        if (event.getEntity() instanceof Player receiver && event.getCause() == EntityDamageEvent.DamageCause.FIRE && isCampfireNearby(receiver)) {
            event.setCancelled(true);
            return;
        }
    }

    private boolean isCampfireNearby(Player player) {
        for (int x = player.getLocation().getBlockX() - 1; x <= player.getLocation().getBlockX() + 1; x++) {
            for (int y = player.getLocation().getBlockY() - 1; y <= player.getLocation().getBlockY() + 1; y++) {
                for (int z = player.getLocation().getBlockZ() - 1; z <= player.getLocation().getBlockZ() + 1; z++) {
                    Block block = player.getWorld().getBlockAt(x, y, z);
                    if (block.getType() == Material.CAMPFIRE || block.getType() == Material.SOUL_CAMPFIRE) return true;
                }
            }
        }
        return false;
    }
}