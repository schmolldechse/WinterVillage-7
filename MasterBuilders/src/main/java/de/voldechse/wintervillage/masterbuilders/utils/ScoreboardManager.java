package de.voldechse.wintervillage.masterbuilders.utils;

import de.voldechse.wintervillage.library.gradient.Gradient;
import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.commands.CommandSkip;
import de.voldechse.wintervillage.masterbuilders.gamestate.Types;
import de.voldechse.wintervillage.masterbuilders.gamestate.list.GameStateVoteThemes;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.awt.*;

public class ScoreboardManager {
    
    private final MasterBuilders plugin;
    private final String displayName;

    public ScoreboardManager() {
        this.plugin = InjectionLayer.ext().instance(MasterBuilders.class);
        this.displayName = "  §l" + Gradient.color("Santas Workshop", Color.GREEN, Color.CYAN) + "  ";
    }

    public void generateScoreboard() {
        Bukkit.getOnlinePlayers().forEach(this::generateScoreboard);
    }

    public void generateScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("mb-scoreboard", "dummy", displayName);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        CommandSkip skipCommand = InjectionLayer.ext().instance(CommandSkip.class);

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase.equals(Types.BUILDING_PHASE)) {
            Score space1 = objective.getScore("§8");
            space1.setScore(10);

            Score time = objective.getScore(" §fZeit§8:");
            time.setScore(9);

            Team ingameTimer = scoreboard.registerNewTeam("ingameTimer");
            ingameTimer.addEntry("§e");
            ingameTimer.setPrefix(" §e00:00");
            objective.getScore("§e").setScore(8);

            Score space2 = objective.getScore("  ");
            space2.setScore(7);

            Score theme = objective.getScore(" §fGebaut wird§8:");
            theme.setScore(6);

            Team currentTheme = scoreboard.registerNewTeam("currentTheme");
            currentTheme.addEntry("§c");
            currentTheme.setPrefix(" §c" + GameStateVoteThemes.winningTheme);
            objective.getScore("§c").setScore(5);

            Score space3 = objective.getScore("    ");
            space3.setScore(4);

            Score skip = objective.getScore(" §fBauzeit überspringen§8:");
            skip.setScore(3);

            Team voteToSkip = scoreboard.registerNewTeam("voteSkips");
            voteToSkip.addEntry("§d");
            voteToSkip.setPrefix(" §d" + CommandSkip.skipVoteList.size() + "§8/§7" + skipCommand.calculateRequiredPlayers(this.plugin.teamManager.getPlayerTeamList().size()) + " §7§o/skip");
            objective.getScore("§d").setScore(2);

            Score space4 = objective.getScore("     ");
            space4.setScore(1);
        } else if (gamePhase.equals(Types.VOTING_BUILDINGS) || gamePhase.equals(Types.VOTING_THEME)) {
            Score space1 = objective.getScore("§8");
            space1.setScore(7);

            Score time = objective.getScore(" §fZeit§8:");
            time.setScore(6);

            Team ingameTimer = scoreboard.registerNewTeam("ingameTimer");
            ingameTimer.addEntry("§e");
            ingameTimer.setPrefix(" §e00:00");
            objective.getScore("§e").setScore(5);

            Score space2 = objective.getScore("  ");
            space2.setScore(4);

            Score theme = objective.getScore(" §fGebaut wird§8:");
            theme.setScore(3);

            Team currentTheme = scoreboard.registerNewTeam("currentTheme");
            currentTheme.addEntry("§c");
            currentTheme.setPrefix(" §c" + GameStateVoteThemes.winningTheme);
            objective.getScore("§c").setScore(2);

            Score space3 = objective.getScore("    ");
            space3.setScore(1);
        }

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
        if (team != null) team.removeEntry(player.getName());
    }

    public Scoreboard getScoreboard(Player player, String metadata) {
        if (player.hasMetadata(metadata) && player.getMetadata(metadata).size() > 0)
            return (Scoreboard) player.getMetadata(metadata).get(0).value();
        return null;
    }
}