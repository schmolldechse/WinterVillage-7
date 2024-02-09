package de.voldechse.wintervillage.ttt.listener;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.game.corpse.CorpseEntity;
import de.voldechse.wintervillage.ttt.game.corpse.PacketReader;
import de.voldechse.wintervillage.ttt.game.corpse.player.CorpseData;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.roles.Role;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;

public class PlayerQuitListener implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @EventHandler
    public void execute(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        Player player = event.getPlayer();

        PacketReader packetReader = new PacketReader(player);
        packetReader.uninject();

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();

        Role role = null;
        if (this.plugin.roleManager.isPlayerAssigned(player))
            role = this.plugin.roleManager.getRole(player);

        Scoreboard scoreboard = player.getScoreboard();
        if (scoreboard.getTeam("1_DETECTIVE") != null
                && scoreboard.getTeam("1_DETECTIVE").hasEntry(player.getName()))
            scoreboard.getTeam("1_DETECTIVE").removeEntry(player.getName());
        if (scoreboard.getTeam("2_INNOCENT") != null
                && scoreboard.getTeam("2_INNOCENT").hasEntry(player.getName()))
            scoreboard.getTeam("2_INNOCENT").removeEntry(player.getName());
        if (scoreboard.getTeam("9_SPECTATOR") != null
                && scoreboard.getTeam("9_SPECTATOR").hasEntry(player.getName()))
            scoreboard.getTeam("9_SPECTATOR").removeEntry(player.getName());

        /**
         * CLOUD IMPLEMENTATION
         */
        if (!plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join()) return;

        PermissionUser permissionUser = plugin.permissionManagement.userAsync(player.getUniqueId()).join();
        if (permissionUser == null) return;
        PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) return;

        switch (gamePhase) {
            case LOBBY -> {
                event.setQuitMessage("§c« " + permissionGroup.color() + permissionUser.name() + " §fhat Winter Village verlassen");
                if (role != null)
                    this.plugin.roleManager.removeFromRole(player, role.roleId);

                if (this.plugin.roleManager.getPlayerList().size() - 1 <= this.plugin.minPlayers) {
                    Countdown countdown = this.plugin.gameStateManager.currentGameState().getCountdown();
                    countdown.stopCountdown(false);
                    countdown.sleepCountdown(15);

                    this.plugin.STARTED = false;

                    Bukkit.getOnlinePlayers().forEach(players -> {
                        players.setExp(0.0f);
                        players.setLevel(0);
                    });

                    Bukkit.broadcastMessage(this.plugin.serverPrefix + "§cSpielstart abgebrochen!");
                }
            }
            case INGAME -> {
                if (!this.plugin.gameManager.isSpectator(player)) {
                    CorpseData corpseData = new CorpseData(
                            player.getLocation(),
                            this.plugin.deadSkinTexture,
                            new Document("diedPlayer_NAME", player.getName())
                                    .append("diedPlayer_UUID", player.getUniqueId())
                                    .append("diedPlayer_PREFIX", role.getRolePrefix())
                                    .append("diedPlayer_ROLE", role.getRolePrefix() + role.getRoleName())
                                    .append("TIMESTAMP", System.currentTimeMillis())
                    );
                    corpseData.setSavedShopPoints(this.plugin.roleManager.getShopPoints(player));
                    corpseData.setIdentified(false);
                    new CorpseEntity(corpseData).spawn(true);

                    this.plugin.roleManager.removeFromRole(player, role.roleId);

                    if (this.plugin.roleManager.getRole(2).getPlayers().size() == 1 && !PlayerDeathListener.LAST_TRAITOR) {
                        Player lastTraitor = this.plugin.roleManager.getRole(2).getPlayers().get(0);

                        //lastTraitor.sendMessage(this.plugin.serverPrefix + "§cDu bist der letzte überlebende Traitor §b+§e1 §4Traitor-Punkt");
                        lastTraitor.sendMessage(this.plugin.serverPrefix + "§cDu bist der letzte überlebende Krampus §b+§e1 §4Krampus-Punkt");
                        this.plugin.roleManager.changeShopPoints(lastTraitor, 1);

                        PlayerDeathListener.LAST_TRAITOR = true;
                    }

                    this.plugin.gameManager.checkGame();
                }
            }

            default -> {

            }
        }

        if (this.plugin.gameManager.isSpectator(player))
            this.plugin.gameManager.removeSpectator(player);
    }
}