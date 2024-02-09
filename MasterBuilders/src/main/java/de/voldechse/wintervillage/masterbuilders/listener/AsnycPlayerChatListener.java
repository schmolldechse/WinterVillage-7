package de.voldechse.wintervillage.masterbuilders.listener;

import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.gamestate.Types;
import de.voldechse.wintervillage.masterbuilders.teams.Team;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsnycPlayerChatListener implements Listener {
    
    private final MasterBuilders plugin = InjectionLayer.ext().instance(MasterBuilders.class);

    @EventHandler
    public void execute(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata("EDIT_TEAM")) {
            event.setCancelled(true);
            Team team = this.plugin.teamManager.getTeam(player.getMetadata("EDIT_TEAM").get(0).asInt());

            switch (event.getMessage().toUpperCase()) {
                case "CORNER_A" -> {
                    this.plugin.teamManager.update(team.teamId, "CORNER_A", player.getLocation());
                    player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                }
                case "CORNER_B" -> {
                    this.plugin.teamManager.update(team.teamId, "CORNER_B", player.getLocation());
                    player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                }
                case "PLAYER_SPAWN" -> {
                    this.plugin.teamManager.update(team.teamId, "PLAYER_SPAWN", player.getLocation());
                    player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                }
                case "VILLAGER_SPAWN" -> {
                    this.plugin.teamManager.update(team.teamId, "VILLAGER_SPAWN", player.getLocation());
                    player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                }
                case "DONE" -> {
                    player.sendMessage(this.plugin.serverPrefix + "§aBearbeitungsmodus für Plot §f" + team.teamId + " §averlassen");
                    this.plugin.removeMetadata(player, "EDIT_TEAM");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
                }
                default -> {
                    player.sendMessage(this.plugin.serverPrefix + "§fSchreibe §eCORNER_A §8| §eCORNER_B §8| §ePLAYER_SPAWN §8| §eVILLAGER_SPAWN §fin den Chat, um die Position zu speichern oder §aDONE §fum den Bearbeitungsmodus zu verlassen");
                    player.playSound(player.getLocation(), Sound.ENTITY_PILLAGER_DEATH, 1.0F, 1.0F);
                }
            }
            return;
        }

        if (!plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join()) {
            event.setCancelled(true);
            player.sendMessage(plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
            return;
        }

        PermissionUser permissionUser = plugin.permissionManagement.userAsync(player.getUniqueId()).join();
        if (permissionUser == null) {
            event.setCancelled(true);
            player.sendMessage(plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
            return;
        }
        PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) {
            event.setCancelled(true);
            player.sendMessage(plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
            return;
        }

        Team team = null;
        if (this.plugin.teamManager.isPlayerInTeam(player))
            team = this.plugin.teamManager.getTeam(player);

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        switch (gamePhase) {
            case LOBBY, RESTART -> {
                event.getRecipients().clear();
                event.getRecipients().addAll(Bukkit.getOnlinePlayers());

                event.setFormat(" " + permissionGroup.color() + permissionGroup.name() + " §f%1$s" + " §8| §f%2$s");
            }

            case VOTING_THEME, BUILDING_PHASE -> {
                if (this.plugin.gameManager.isSpectator(player)) {
                    event.getRecipients().clear();
                    event.getRecipients().addAll(this.plugin.gameManager.getSpectatorList());

                    event.setFormat(" §4✘ " + permissionGroup.color() + permissionGroup.name() + " §f%1$s §8| §f%2$s");
                    return;
                }

                if (event.getMessage().startsWith("@all ") || event.getMessage().startsWith("@a ")) {
                    event.setMessage(event.getMessage().replaceFirst("@all ", "")
                            .replaceFirst("@a ", ""));

                    event.getRecipients().clear();
                    event.getRecipients().addAll(Bukkit.getOnlinePlayers());

                    event.setFormat(" §c@all " + permissionGroup.color() + permissionGroup.name() + " §f%1$s" + " §8| §f%2$s");
                    return;
                }

                event.getRecipients().clear();
                event.getRecipients().addAll(team.plotOwner);

                event.setFormat(" " + permissionGroup.color() + permissionGroup.name() + " §f%1$s §8| §f%2$s");
            }

            case VOTING_BUILDINGS -> {
                if (this.plugin.gameManager.isSpectator(player)) {
                    event.getRecipients().clear();
                    event.getRecipients().addAll(this.plugin.gameManager.getSpectatorList());

                    event.setFormat(" §4✘ " + permissionGroup.color() + "%1$s §8| §f%2$s");
                    return;
                }

                event.getRecipients().clear();
                event.getRecipients().addAll(Bukkit.getOnlinePlayers());

                event.setFormat(" " + permissionGroup.color() + permissionGroup.name() + " §f%1$s §8| §f%2$s");
            }

            default -> {
            }
        }
    }
}