package de.voldechse.wintervillage.aura.utils;

import de.voldechse.wintervillage.aura.Aura;
import de.voldechse.wintervillage.library.gradient.Gradient;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.awt.*;

public class ScoreboardManager {

    private final Aura plugin;

    private final String displayName;

    public ScoreboardManager() {
        this.plugin = InjectionLayer.ext().instance(Aura.class);
        this.displayName = "  " + Gradient.color("Aura", Color.WHITE, Color.RED) + "  ";
    }

    public void generateScoreboard() {
        Bukkit.getOnlinePlayers().forEach(this::generateScoreboard);
    }

    public void generateScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("aura-scoreboard", "dummy", this.displayName);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Score space1 = objective.getScore(" ");
        space1.setScore(12);

        Score livingPlayers = objective.getScore(" §fSpieler§8:");
        livingPlayers.setScore(11);

        Team currentPlayers = scoreboard.registerNewTeam("currentPlayers");
        currentPlayers.addEntry("§b");
        currentPlayers.setPrefix(" §a-§8/§a-");
        objective.getScore("§b").setScore(10);

        Score space2 = objective.getScore("  ");
        space2.setScore(9);

        Score timeLeft = objective.getScore(" §fZeit§8:");
        timeLeft.setScore(8);

        Team ingameTimer = scoreboard.registerNewTeam("ingameTimer");
        ingameTimer.addEntry("§e");
        ingameTimer.setPrefix(" §e--:--");
        objective.getScore("§e").setScore(7);

        Score space3 = objective.getScore("   ");
        space3.setScore(6);

        Team borderPhase = scoreboard.registerNewTeam("currentPhase");
        borderPhase.addEntry("§9");
        borderPhase.setPrefix(" §fNächste Border§8:");
        objective.getScore("§9").setScore(5);

        Team phaseSize = scoreboard.registerNewTeam("phaseSize");
        phaseSize.addEntry("§8");
        phaseSize.setPrefix(" §c-§8x§c-");
        objective.getScore("§8").setScore(4);

        Team phaseSeconds = scoreboard.registerNewTeam("phaseSeconds");
        phaseSeconds.addEntry("§f");
        phaseSeconds.setPrefix(" §c--:--");
        objective.getScore("§f").setScore(3);

        Score space4 = objective.getScore("    ");
        space4.setScore(2);

        Score forbiddenTeams = objective.getScore(" §cTEAMS VERBOTEN");
        forbiddenTeams.setScore(1);

        player.setScoreboard(scoreboard);

        this.plugin.setMetadata(player, "SCOREBOARD_SIDEBAR", scoreboard);
    }

    public void updateScoreboard(String scoreboardTeam, String prefix, String suffix) {
        Bukkit.getOnlinePlayers().forEach(player -> updateScoreboard(player, scoreboardTeam, prefix, suffix));
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

        Team SPECTATOR = scoreboard.getTeam("999_SPECTATOR");
        if (SPECTATOR == null) SPECTATOR = scoreboard.registerNewTeam("999_SPECTATOR");
        SPECTATOR.setColor(ChatColor.GRAY);

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (this.plugin.gameManager.isSpectator(online)) {
                SPECTATOR.addEntry(online.getName());
                online.setPlayerListName(SPECTATOR.getPrefix() + online.getName());
                continue;
            }

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

    public Scoreboard getScoreboard(Player player, String metadata) {
        if (player.hasMetadata(metadata) && player.getMetadata(metadata).size() > 0)
            return (Scoreboard) player.getMetadata(metadata).get(0).value();
        return null;
    }
}