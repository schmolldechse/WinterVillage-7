package de.voldechse.wintervillage.util;

import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.library.gradient.Gradient;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.awt.*;

public class ScoreboardManager {

    private final WinterVillage plugin;

    public ScoreboardManager(WinterVillage plugin) {
        this.plugin = plugin;
    }

    public void generateScoreboard() {
        Bukkit.getOnlinePlayers().forEach(this::generateScoreboard);
    }

    public void generateScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("wv-scoreboard", Criteria.DUMMY, "\uD83D\uDD61");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        Score space_1 = objective.getScore("");
        space_1.setScore(10);

        Score online = objective.getScore(" §fOnline§8:");
        online.setScore(9);

        Team currentOnlineTeam;
        if (scoreboard.getTeam("currentOnline") == null)
            currentOnlineTeam = scoreboard.registerNewTeam("currentOnline");
        else currentOnlineTeam = scoreboard.getTeam("currentOnline");

        currentOnlineTeam.addEntry("§a");
        currentOnlineTeam.setPrefix(" §a--");
        objective.getScore("§a").setScore(8);

        Score space_2 = objective.getScore(" ");
        space_2.setScore(7);

        Score balance = objective.getScore(" §fKontostand§8:");
        balance.setScore(6);

        Team currentBalanceTeam;
        if (scoreboard.getTeam("currentBalance") == null)
            currentBalanceTeam = scoreboard.registerNewTeam("currentBalance");
        else currentBalanceTeam = scoreboard.getTeam("currentBalance");

        currentBalanceTeam.addEntry("§e");
        currentBalanceTeam.setPrefix(" §e-- $");
        objective.getScore("§e").setScore(5);

        Score space_3 = objective.getScore("  ");
        space_3.setScore(4);

        Score rank = objective.getScore(" §fRang§8:");
        rank.setScore(3);

        Team currentRankTeam;
        if (scoreboard.getTeam("currentRank") == null) currentRankTeam = scoreboard.registerNewTeam("currentRank");
        else currentRankTeam = scoreboard.getTeam("currentRank");

        currentRankTeam.addEntry("§c");
        currentRankTeam.setPrefix(" §c--");
        objective.getScore("§c").setScore(2);

        Score space_4 = objective.getScore("   ");
        space_4.setScore(1);

        Score domain = objective.getScore(" §b§lwintervillage.de");
        domain.setScore(0);

        player.setScoreboard(scoreboard);
    }

    public void playerList() { Bukkit.getOnlinePlayers().forEach(this::playerList); }

    public void playerList(Player player) {
        Scoreboard scoreboard = player.getScoreboard();

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

            if (this.plugin.clanSystem.getCUtils().isInClan(online.getUniqueId().toString())) {
                String clan = this.plugin.clanSystem.getCUtils().getClan(online.getUniqueId().toString());
                online.setPlayerListName(team.getPrefix() + online.getName() + " §8[§a" + this.plugin.clanSystem.getCUtils().getClanTag(clan) + "§8]");
            } else online.setPlayerListName(team.getPrefix() + online.getName());
        }
    }

    public void updateScoreboard(String scoreboardTeam, String valueToChange, String suffix) {
        Bukkit.getOnlinePlayers().forEach(player -> updateScoreboard(player, scoreboardTeam, valueToChange, suffix));
    }

    public void updateScoreboard(Player player, String scoreboardTeam, String prefix, String suffix) {
        Scoreboard scoreboardToUpdate = player.getScoreboard();
        if (scoreboardToUpdate == null) generateScoreboard(player);
        scoreboardToUpdate.getTeam(scoreboardTeam).setPrefix(prefix);
        scoreboardToUpdate.getTeam(scoreboardTeam).setSuffix(suffix);
    }
}