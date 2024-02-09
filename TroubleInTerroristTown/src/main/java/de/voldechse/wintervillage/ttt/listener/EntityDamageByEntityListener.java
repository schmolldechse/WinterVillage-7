package de.voldechse.wintervillage.ttt.listener;

import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.roles.Role;
import de.voldechse.wintervillage.ttt.utils.position.PositionEntity;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Objects;
import java.util.UUID;

public class EntityDamageByEntityListener implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @EventHandler
    public void execute(EntityDamageByEntityEvent event) {
        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();

        if (event.getEntity() instanceof Player receiver && event.getDamager() instanceof Player damager) {
            if (gamePhase != Types.INGAME) {
                event.setCancelled(true);
                return;
            }

            if (this.plugin.gameManager.isSpectator(damager)) {
                event.setCancelled(true);
                return;
            }

            if (this.plugin.gameManager.isSpectator(receiver)) {
                event.setCancelled(true);
                return;
            }

            if (this.plugin.roleManager.getRole(damager) != null && this.plugin.roleManager.getRole(receiver) != null) {
                Role damagerRole = this.plugin.roleManager.getRole(damager);
                Role receiverRole = this.plugin.roleManager.getRole(receiver);

                if (damager.getInventory().getItemInMainHand().getType() == Material.TRIDENT) {
                    event.setCancelled(true);
                    return;
                }

                if (damagerRole.roleId == 1 && receiverRole.roleId == 1) event.setDamage(0);
                if (damagerRole.roleId == 2 && receiverRole.roleId == 2) event.setDamage(0);

                event.setCancelled(false);
                return;
            }

            event.setCancelled(true);
        } else if (event.getDamager() instanceof Arrow
                && event.getEntity() instanceof Player receiver
                && ((Arrow) event.getDamager()).getShooter() instanceof Player shooter) {
            if (gamePhase != Types.INGAME) {
                event.setCancelled(true);
                return;
            }

            if (this.plugin.gameManager.isSpectator(shooter)) {
                event.setCancelled(true);
                return;
            }

            if (this.plugin.gameManager.isSpectator(receiver)) {
                event.setCancelled(true);
                return;
            }

            if (this.plugin.roleManager.getRole(shooter) != null && this.plugin.roleManager.getRole(receiver) != null) {
                Role damagerRole = this.plugin.roleManager.getRole(shooter);
                Role receiverRole = this.plugin.roleManager.getRole(receiver);

                if (damagerRole.roleId == 1 && receiverRole.roleId == 1) event.setDamage(0);
                if (damagerRole.roleId == 2 && receiverRole.roleId == 2) event.setDamage(0);

                event.setCancelled(false);
                return;
            }

            event.setCancelled(true);
        } else if (event.getDamager() instanceof Player damager && event.getEntity() instanceof ArmorStand armorStand) {
            if (damager.hasMetadata("BUILD_MODE")) {
                event.setCancelled(false);
                return;
            }

            if (armorStand.getCustomName() != null && damager.hasMetadata("EDIT_SPAWNS")) {
                UUID uuid = UUID.fromString(ChatColor.stripColor(armorStand.getCustomName()));
                if (!this.plugin.positionManager.isSpawnSaved(uuid)) {
                    damager.sendMessage(this.plugin.serverPrefix + "§cDiese Position ist nicht gespeichert");
                    event.setCancelled(true);
                    return;
                }

                PositionEntity position = this.plugin.positionManager.getPositionFromConfig(uuid);
                damager.sendMessage(this.plugin.serverPrefix + "§eDu hast die Position bei §c" + position.toString() + " §egelöscht");
                damager.playSound(damager.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                this.plugin.positionManager.removeSpawnConfig(uuid);

                armorStand.remove();
                event.setCancelled(false);
                return;
            }

            event.setCancelled(true);
        } else if (event.getEntity() instanceof Player receiver
                && event.getDamager().hasMetadata("SUMMONED_BY")
                && !(event.getDamager() instanceof Player)) {
            if (gamePhase != Types.INGAME) {
                event.setCancelled(true);
                return;
            }

            if (this.plugin.gameManager.isSpectator(receiver)) {
                event.setCancelled(true);
                return;
            }

            UUID summonedBy = Objects.requireNonNull((UUID) event.getDamager().getMetadata("SUMMONED_BY").get(0).value());
            if (Bukkit.getPlayer(summonedBy) == null) return;

            this.plugin.setMetadata(receiver, "LAST_DAMAGER", summonedBy);
            //} else if (event.getEntity() instanceof Creeper || event.getEntity() instanceof Vex) {
        } else {
            if (gamePhase != Types.INGAME) {
                event.setCancelled(true);
                return;
            }

            Entity damager = event.getDamager();

            if (damager instanceof Arrow arrow) {
                if (arrow.getShooter() instanceof Player shooter && this.plugin.gameManager.isSpectator(shooter)) {
                    event.setCancelled(true);
                    return;
                }

                event.setCancelled(false);
            } else if (damager instanceof Player) {
                if (this.plugin.gameManager.isSpectator((Player) damager)) {
                    event.setCancelled(true);
                    return;
                }

                event.setCancelled(false);
            }
        }
    }
}