package de.voldechse.wintervillage.masterbuilders.gamestate.list;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.countdown.CountdownListener;
import de.voldechse.wintervillage.library.gradient.Gradient;
import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.gamestate.GameState;
import de.voldechse.wintervillage.masterbuilders.gamestate.Types;
import de.voldechse.wintervillage.masterbuilders.teams.Team;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameStateRestart extends GameState {
    
    private final MasterBuilders plugin = InjectionLayer.ext().instance(MasterBuilders.class);

    public static final int RESTART_TIME = 20;

    private Countdown countdown;

    @Override
    public void startCountdown() {
        this.countdown = new Countdown(this.plugin.getInstance(), new CountdownListener() {
            @Override
            public void start() {
                plugin.teamManager.sortByPoints(plugin.teamManager.getTeamList());

                List<Team> teamsWon = getTeamsWithMostPoints();

                Team teamToTeleport = teamsWon.get(0);
                Location location = new Location(
                        Bukkit.getWorld(teamToTeleport.playerSpawn.getWorld()),
                        teamToTeleport.playerSpawn.getX(),
                        teamToTeleport.playerSpawn.getY(),
                        teamToTeleport.playerSpawn.getZ(),
                        teamToTeleport.playerSpawn.getYaw(),
                        teamToTeleport.playerSpawn.getPitch()
                );
                Bukkit.getOnlinePlayers().forEach(player -> player.teleport(location.clone().add(0, 10, 0)));

                if (teamsWon.size() == 1) {
                    StringBuilder stringBuilder = new StringBuilder();
                    if (teamToTeleport.plotOwner.size() > 0) {
                        for (int i = 0; i < teamToTeleport.plotOwner.size(); i++) {
                            if (!plugin.permissionManagement.containsUserAsync(teamToTeleport.plotOwner.get(i).getUniqueId()).join()) continue;
                            PermissionUser permissionUser = plugin.permissionManagement.userAsync(teamToTeleport.plotOwner.get(i).getUniqueId()).join();
                            if (!plugin.permissionManagement.containsUserAsync(permissionUser.uniqueId()).join()) continue;
                            PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);

                            stringBuilder.append(permissionGroup.color()).append(permissionUser.name());
                            if (teamToTeleport.plotOwner.size() > 1 && i < teamToTeleport.plotOwner.size() - 1)
                                stringBuilder.append("§8, ");
                        }
                    }

                    Bukkit.broadcastMessage(plugin.serverPrefix + "§b" + stringBuilder.toString() + (teamToTeleport.plotOwner.size() == 1 ? " §ehat" : " §ehaben") + " das Spiel gewonnen!");
                    plugin.gameManager.broadcastTitle(stringBuilder.toString(), (teamToTeleport.plotOwner.size() == 1 ? " §ehat" : " §ehaben") + " gewonnen§8!");
                } else {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < teamsWon.size(); i++) {
                        Team team = teamsWon.get(i);
                        for (int j = 0; j < team.plotOwner.size(); j++) {
                            if (!plugin.permissionManagement.containsUserAsync(team.plotOwner.get(j).getUniqueId()).join()) continue;
                            PermissionUser permissionUser = plugin.permissionManagement.userAsync(team.plotOwner.get(j).getUniqueId()).join();
                            if (!plugin.permissionManagement.containsUserAsync(permissionUser.uniqueId()).join()) continue;
                            PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);

                            stringBuilder.append(permissionGroup.color()).append(permissionUser.name());
                            if (j < team.plotOwner.size() - 1) stringBuilder.append("§8, ");
                        }
                        if (i < teamsWon.size() - 1) stringBuilder.append(" §eund ");
                    }

                    Bukkit.broadcastMessage(plugin.serverPrefix + stringBuilder.toString() + " §ehaben das Spiel gewonnen!");
                }

                Bukkit.broadcastMessage("§8--------------------");
                plugin.teamManager.getTeamList().forEach(team -> {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < team.plotOwner.size(); i++) {
                        if (!plugin.permissionManagement.containsUserAsync(team.plotOwner.get(i).getUniqueId()).join()) continue;
                        PermissionUser permissionUser = plugin.permissionManagement.userAsync(team.plotOwner.get(i).getUniqueId()).join();
                        if (!plugin.permissionManagement.containsUserAsync(permissionUser.uniqueId()).join()) continue;
                        PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);

                        stringBuilder.append(permissionGroup.color()).append(permissionUser.name());

                        if (team.plotOwner.size() > 1 && i < team.plotOwner.size() - 1)
                            stringBuilder.append("§8, ");
                    }
                    Bukkit.broadcastMessage("§d" + plugin.teamManager.calculateTeamRanking(team) + "§8. §b" + stringBuilder.toString() + "§8: §e" + team.earnedVotepoints + " §7Punkte");
                });

                //TODO: if (teamsWon.size() == 1 && plugin.maxPlayersInTeam == 1) generateScoreboard();
            }

            @Override
            public void stop() {
                Bukkit.broadcastMessage(plugin.serverPrefix + "§cDer Server startet nun neu");
                endCountdown();
            }

            @Override
            public void second(int i) {
                switch (i) {
                    case 10, 5, 3, 2 ->
                            Bukkit.broadcastMessage(plugin.serverPrefix + "§cDer Server startet in §b" + i + " §cSekunden neu");
                    case 1 ->
                            Bukkit.broadcastMessage(plugin.serverPrefix + "§cDer Server startet in §b" + i + " §cSekunde neu");
                    default -> {
                    }
                }
            }

            @Override
            public void sleep() {
            }
        });
        this.countdown.startCountdown(GameStateRestart.RESTART_TIME, false);
    }

    @Override
    public void endCountdown() {
        Bukkit.getScheduler().runTaskLater(this.plugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                Bukkit.getServer().shutdown();
            }
        }, 40L);
    }

    @Override
    public Types getGameStatePhase() {
        return Types.RESTART;
    }

    @Override
    public Countdown getCountdown() {
        return this.countdown;
    }

    private List<Team> getTeamsWithMostPoints() {
        List<Team> teamsWithMostPoints = new ArrayList<Team>();
        int highestPoints = -1;

        for (Team team : this.plugin.teamManager.getTeamList()) {
            int earnedPoints = team.earnedVotepoints;
            if (earnedPoints >= highestPoints) {
                highestPoints = earnedPoints;
                teamsWithMostPoints.add(team);
            }
        }

        return teamsWithMostPoints;
    }

    //TODO: recode
    private void generateScoreboard() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("mb-scoreboard_restart", "dummy", "  §l" + Gradient.color("Santas Workshop", Color.GREEN, Color.CYAN) + "  ");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Score space1 = objective.getScore("§9");
        space1.setScore(9999);
        this.plugin.teamManager.getTeamList().forEach(team -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < team.plotOwner.size(); i++) {
                stringBuilder.append(team.plotOwner.get(i).getName());
                if (team.plotOwner.size() > 1 && i < team.plotOwner.size() - 1)
                    stringBuilder.append(", ");
            }

            Score teamScore = objective.getScore("§7" + this.plugin.teamManager.calculateTeamRanking(team) + "§8. §a" + stringBuilder.toString());
            teamScore.setScore(team.earnedVotepoints);
        });

        Score space2 = objective.getScore("§c");
        space2.setScore(0);

        Bukkit.getOnlinePlayers().forEach(player -> player.setScoreboard(scoreboard));
    }
}