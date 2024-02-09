package de.voldechse.wintervillage.ttt.utils;

import de.voldechse.wintervillage.library.gradient.Gradient;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.roles.Role;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.awt.*;

public class ScoreboardManager {
    
    private final TTT plugin;
    private final String displayName;

    public ScoreboardManager() {
        this.plugin = InjectionLayer.ext().instance(TTT.class);
        
        this.displayName = "  §l" + Gradient.color("Krampus Hunt", Color.RED, Color.YELLOW) + "  ";
    }

    public void generateScoreboard() {
        Bukkit.getOnlinePlayers().forEach(this::generateScoreboard);
    }

    public void generateScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("ttt-scoreboard", "dummy", displayName);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();

        scoreboard.getTeams().forEach(team -> {
            if (team.hasEntry(player.getName())) team.removeEntry(player.getName());
        });

        Team DETECTIVE = scoreboard.getTeam("1_DETECTIVE");
        if (DETECTIVE == null) DETECTIVE = scoreboard.registerNewTeam("1_DETECTIVE");
        DETECTIVE.setPrefix("§9");
        DETECTIVE.setColor(ChatColor.BLUE);

        Team INNOCENT = scoreboard.getTeam("2_INNOCENT");
        if (INNOCENT == null) INNOCENT = scoreboard.registerNewTeam("2_INNOCENT");
        INNOCENT.setPrefix("§a");
        INNOCENT.setColor(ChatColor.GREEN);

        Team TRAITOR = scoreboard.getTeam("3_TRAITOR");
        if (TRAITOR == null) TRAITOR = scoreboard.registerNewTeam("3_TRAITOR");
        TRAITOR.setPrefix("§4");
        TRAITOR.setColor(ChatColor.DARK_RED);

        Team SPECTATOR = scoreboard.getTeam("9_SPECTATOR");
        if (SPECTATOR == null) SPECTATOR = scoreboard.registerNewTeam("9_SPECTATOR");
        SPECTATOR.setPrefix("§7");
        SPECTATOR.setColor(ChatColor.GRAY);

        switch (gamePhase) {
            case PREPARING_START -> {
                Score space1 = objective.getScore(" ");
                space1.setScore(6);

                Score playerScore = objective.getScore(" §fSpieler§8:");
                playerScore.setScore(5);

                Team players = scoreboard.registerNewTeam("currentPlayers");
                players.addEntry("§a");
                players.setPrefix(" §a0§8/§a0");
                objective.getScore("§a").setScore(4);

                Score space2 = objective.getScore("  ");
                space2.setScore(3);

                Score time = objective.getScore(" §fSchutzzeit§8:");
                time.setScore(2);

                Team ingameTimer = scoreboard.registerNewTeam("ingameTimer");
                ingameTimer.addEntry("§e");
                ingameTimer.setPrefix(" §e00:00");
                objective.getScore("§e").setScore(1);

                Score space3 = objective.getScore("   ");
                space3.setScore(0);
            }
            case INGAME -> {
                if (this.plugin.gameManager.isSpectator(player) && !this.plugin.roleManager.isPlayerAssigned(player)) {
                    Score space1 = objective.getScore("§8");
                    space1.setScore(6);

                    Score playerScore = objective.getScore(" §fSpieler§8:");
                    playerScore.setScore(5);

                    Team players = scoreboard.registerNewTeam("currentPlayers");
                    players.addEntry("§a");
                    players.setPrefix(" §a0§8/§a0");
                    objective.getScore("§a").setScore(4);

                    Score space2 = objective.getScore("  ");
                    space2.setScore(3);

                    Score end = objective.getScore(" §fEnde§8:");
                    end.setScore(2);

                    Team ingameTimer = scoreboard.registerNewTeam("ingameTimer");
                    ingameTimer.addEntry("§e");
                    ingameTimer.setPrefix(" §e00:00");
                    objective.getScore("§e").setScore(1);

                    Score space3 = objective.getScore("   ");
                    space3.setScore(0);
                } else {
                    Role role = this.plugin.roleManager.getRole(player);
                    if (role == null) return;

                    Score space1 = objective.getScore(" ");
                    space1.setScore(9);

                    Score shopPoints = null;
                    /**
                     if (role.roleId == 1)
                     shopPoints = objective.getScore(" §fD-Punkte§8:");
                     else if (role.roleId == 2)
                     shopPoints = objective.getScore(" §fT-Punkte§8:");
                     */
                    shopPoints = objective.getScore(" §fPunkte§8:");
                    shopPoints.setScore(8);

                    Team currentShopPoints = scoreboard.registerNewTeam("currentShopPoints");
                    currentShopPoints.addEntry("§b");
                    /**
                     if (role.roleId == 1)
                     currentShopPoints.setPrefix(" §9" + this.plugin.roleManager.getShopPoints(player));
                     else if (role.roleId == 2)
                     currentShopPoints.setPrefix(" §4" + this.plugin.roleManager.getShopPoints(player));
                     */
                    currentShopPoints.setPrefix(" " + role.getRolePrefix() + this.plugin.roleManager.getShopPoints(player));

                    objective.getScore("§b").setScore(7);

                    Score space2 = objective.getScore("§8");
                    space2.setScore(6);

                    Score playerScore = objective.getScore(" §fSpieler§8:");
                    playerScore.setScore(5);

                    Team players = scoreboard.registerNewTeam("currentPlayers");
                    players.addEntry("§a");
                    players.setPrefix(" §a0§8/§a0");
                    objective.getScore("§a").setScore(4);

                    Score space3 = objective.getScore("   ");
                    space3.setScore(3);

                    Score end = objective.getScore(" §fEnde§8:");
                    end.setScore(2);

                    Team ingameTimer = scoreboard.registerNewTeam("ingameTimer");
                    ingameTimer.addEntry("§e");
                    ingameTimer.setPrefix(" §e00:00");
                    objective.getScore("§e").setScore(1);

                    Score space4 = objective.getScore("    ");
                    space4.setScore(0);
                }

                for (Player online : Bukkit.getOnlinePlayers()) {
                    if (this.plugin.gameManager.isSpectator(online) || this.plugin.roleManager.getRole(online) == null) {
                        SPECTATOR.addEntry(online.getName());
                        online.setPlayerListName(SPECTATOR.getPrefix() + online.getName());
                        continue;
                    }

                    boolean display_traitor_as_innocent = this.plugin.gameManager.isSpectator(player)
                            || (this.plugin.roleManager.isPlayerAssigned(player) && this.plugin.roleManager.getRole(player).roleId != 2);

                    Role role = this.plugin.roleManager.getRole(online);

                    switch (role.roleId) {
                        case 0 -> {
                            INNOCENT.addEntry(online.getName());
                            online.setPlayerListName(INNOCENT.getPrefix() + online.getName());
                            continue;
                        }

                        case 1 -> {
                            DETECTIVE.addEntry(online.getName());
                            online.setPlayerListName(DETECTIVE.getPrefix() + online.getName());
                            continue;
                        }

                        case 2 -> {

                            if (online.hasMetadata("BUSTED_TRAITOR")) {

                            }

                            if (display_traitor_as_innocent && !online.hasMetadata("BUSTED_TRAITOR")) {
                                INNOCENT.addEntry(online.getName());
                                online.setPlayerListName(INNOCENT.getPrefix() + online.getName());
                                continue;
                            }

                            TRAITOR.addEntry(online.getName());
                            online.setPlayerListName(TRAITOR.getPrefix() + online.getName());
                        }
                    }
                }
            }
        }

        player.setScoreboard(scoreboard);

        this.plugin.setMetadata(player, "SCOREBOARD_SIDEBAR", scoreboard);
    }

    public void updateScoreboard(String scoreboardTeam, String valueToChange, String suffix) {
        Bukkit.getOnlinePlayers().forEach(player -> updateScoreboard(player, scoreboardTeam, valueToChange, suffix));
    }

    public void updateScoreboard(Player player, String scoreboardTeam, String prefix, String suffix) {
        if (player.isDead()) return;

        Scoreboard scoreboardToUpdate = getScoreboard(player, "SCOREBOARD_SIDEBAR");
        if (scoreboardToUpdate == null) generateScoreboard(player);
        scoreboardToUpdate.getTeam(scoreboardTeam).setPrefix(prefix);
        scoreboardToUpdate.getTeam(scoreboardTeam).setSuffix(suffix);
    }

    public void playerList() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        plugin.permissionManagement.groupsAsync().join().forEach(permissionGroup -> {
            Team team = scoreboard.getTeam(permissionGroup.sortId() + "_" + permissionGroup.name().toUpperCase());
            if (team == null) team = scoreboard.registerNewTeam(permissionGroup.sortId() + "_" + permissionGroup.name().toUpperCase());

            team.setPrefix(permissionGroup.color().replace("&", "§") + permissionGroup.name() + " §8| §r");
            //if (ChatColor.getByChar(permissionGroup.color().charAt(1)) != null)
            //    team.setColor(ChatColor.getByChar(permissionGroup.color().charAt(1)));
            team.setColor(ChatColor.WHITE);
        });

        for (Player online : Bukkit.getOnlinePlayers()) {
            PermissionUser onlineUser = this.plugin.permissionManagement.userAsync(online.getUniqueId()).join();
            if (onlineUser == null) continue;

            PermissionGroup onlinesGroup = this.plugin.permissionManagement.highestPermissionGroup(onlineUser);
            if (onlinesGroup == null) continue;

            Team team = scoreboard.getTeam(onlinesGroup.sortId() + "_" + onlinesGroup.name().toUpperCase());
            if (team == null) continue;

            team.addEntry(online.getName());

            online.setPlayerListName(team.getPrefix() + online.getName());
        }
    }

    public void removeEntry(Player player, String teamName) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(teamName);
        if (team != null && team.hasEntry(player.getName())) team.removeEntry(player.getName());
    }

    public Scoreboard getScoreboard(Player player, String metadata) {
        if (player.hasMetadata(metadata) && player.getMetadata(metadata).size() > 0)
            return (Scoreboard) player.getMetadata(metadata).get(0).value();
        return null;
    }
}