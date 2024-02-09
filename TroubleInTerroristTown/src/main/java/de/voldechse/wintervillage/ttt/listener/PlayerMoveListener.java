package de.voldechse.wintervillage.ttt.listener;

import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.game.corpse.CorpseEntity;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerMoveListener implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    public static List<UUID> nearCorpse = new ArrayList<>();

    @EventHandler
    public void execute(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();

        if ((gamePhase == Types.PREPARING_START
                && this.plugin.gameStateManager.currentGameState().getCountdown().getCountdownTime() >= 30)
                || player.hasMetadata("SNOWMAN")) {
            Location from = event.getFrom();
            Location to = event.getTo();
            double x = Math.floor(from.getX());
            double z = Math.floor(from.getZ());
            if (Math.floor(to.getX()) != x || Math.floor(to.getZ()) != z) {
                x += .5;
                z += .5;
                player.teleport(new Location(from.getWorld(), x, from.getY(), z, from.getYaw(), from.getPitch()));
                player.teleport(player);
            }
        }

        if (gamePhase == Types.INGAME
                && !this.plugin.gameManager.isSpectator(player)
                && this.plugin.roleManager.getRole(player) != null
                && this.plugin.roleManager.getRole(player).roleId == 1) {
            for (Entity entity : player.getNearbyEntities(15, 1.0, 15D)) {
                if (!entity.hasMetadata("CORPSE_entityId")) continue;

                int entityId = entity.getMetadata("CORPSE_entityId").get(0).asInt();
                CorpseEntity corpse = this.plugin.CORPSES_MAP.get(entityId);
                if (corpse == null) return;

                if (corpse.corpseData.isIdentified()) return;

                double distance = entity.getLocation().distance(player.getLocation());
                double MAX_DISTANCE = 7.5D;

                if (distance < MAX_DISTANCE) {
                    if (nearCorpse.contains(player.getUniqueId())) return;
                    nearCorpse.add(player.getUniqueId());
                    return;
                }

                nearCorpse.remove(player.getUniqueId());
            }
        }
    }
}