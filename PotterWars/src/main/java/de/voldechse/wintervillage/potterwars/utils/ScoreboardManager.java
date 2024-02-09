package de.voldechse.wintervillage.potterwars.utils;

import de.voldechse.wintervillage.library.gradient.Gradient;
import de.voldechse.wintervillage.potterwars.PotterWars;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.awt.*;

public class ScoreboardManager {

    private final String displayName;

    public ScoreboardManager() {
        this.displayName = "  §l" + Gradient.color("ElfWars", Color.WHITE, Color.CYAN) + "  ";
    }

    public void generateScoreboard() {
        Bukkit.getOnlinePlayers().forEach(this::generateScoreboard);
    }

    public void generateScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("pw-scoreboard", "dummy", this.displayName);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        PotterWars.getInstance().teamManager.teamList.forEach(team -> {
            Team scoreboardTeam = scoreboard.getTeam(team.teamId + "_" + team.teamName.toUpperCase());
            if (scoreboardTeam == null) scoreboard.registerNewTeam(team.teamId + "_" + team.teamName.toUpperCase());
        });

        Team SPECTATOR = scoreboard.getTeam("999_SPECTATOR");
        if (SPECTATOR == null) SPECTATOR = scoreboard.registerNewTeam("999_SPECTATOR");
        SPECTATOR.setColor(ChatColor.GRAY);

        if (PotterWars.getInstance().gameManager.isSpectator(player)) {
            Score space1 = objective.getScore(" ");
            space1.setScore(12);

            Score livingPlayers = objective.getScore(" §fSpieler§8:");
            livingPlayers.setScore(11);

            Team currentPlayers = scoreboard.registerNewTeam("currentPlayers");
            currentPlayers.addEntry("§a");
            currentPlayers.setPrefix(" §a-§8/§a-");
            objective.getScore("§a").setScore(10);

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

            Team phaseSeconds = scoreboard.registerNewTeam("phaseSeconds");
            phaseSeconds.addEntry("§f");
            phaseSeconds.setPrefix(" §c--:--");
            objective.getScore("§f").setScore(4);

            Team phaseSize = scoreboard.registerNewTeam("phaseSize");
            phaseSize.addEntry("§8");
            phaseSize.setPrefix(" §c-§8x§c-");
            objective.getScore("§8").setScore(3);

            Score space4 = objective.getScore("    ");
            space4.setScore(2);

            Score forbiddenTeams = objective.getScore(" §cTEAMS VERBOTEN");
            forbiddenTeams.setScore(1);
        } else {
            Score space1 = objective.getScore(" ");
            space1.setScore(15);

            Score livingPlayers = objective.getScore(" §fSpieler§8:");
            livingPlayers.setScore(14);

            Team currentPlayers = scoreboard.registerNewTeam("currentPlayers");
            currentPlayers.addEntry("§b");
            currentPlayers.setPrefix(" §a-§8/§a-");
            objective.getScore("§b").setScore(13);

            Score space2 = objective.getScore("  ");
            space2.setScore(12);

            Score timeLeft = objective.getScore(" §fZeit§8:");
            timeLeft.setScore(11);

            Team ingameTimer = scoreboard.registerNewTeam("ingameTimer");
            ingameTimer.addEntry("§e");
            ingameTimer.setPrefix(" §e--:--");
            objective.getScore("§e").setScore(10);

            Score space3 = objective.getScore("   ");
            space3.setScore(9);

            Team borderPhase = scoreboard.registerNewTeam("currentPhase");
            borderPhase.addEntry("§9");
            borderPhase.setPrefix(" §fNächste Border§8:");
            objective.getScore("§9").setScore(8);

            Team phaseSeconds = scoreboard.registerNewTeam("phaseSeconds");
            phaseSeconds.addEntry("§f");
            phaseSeconds.setPrefix(" §c--:--");
            objective.getScore("§f").setScore(7);

            Team phaseSize = scoreboard.registerNewTeam("phaseSize");
            phaseSize.addEntry("§8");
            phaseSize.setPrefix(" §c-§8x§c-");
            objective.getScore("§8").setScore(6);

            Score space4 = objective.getScore("    ");
            space4.setScore(5);

            Score playerSpell = objective.getScore(" §fZauber§8:");
            playerSpell.setScore(4);

            Team currentSpell = scoreboard.registerNewTeam("currentSpell");
            currentSpell.addEntry("§c");
            currentSpell.setPrefix(" §cKeinen ausgewählt");
            //currentSpell.setPrefix(" " + (PotterWars.getInstance().spellManager.hasSpellChoosed(player) ? PotterWars.getInstance().getSpellManager().getCurrentSpell(player).getName() : "§cKeinen ausgewählt"));
            objective.getScore("§c").setScore(3);

            Score space5 = objective.getScore("     ");
            space5.setScore(2);

            Score forbiddenTeams = objective.getScore(" §cTEAMS VERBOTEN");
            forbiddenTeams.setScore(1);
        }

        for (Player online : Bukkit.getOnlinePlayers()) {
            de.voldechse.wintervillage.potterwars.team.Team team = PotterWars.getInstance().teamManager.getTeam(online);

            if (team == null || PotterWars.getInstance().gameManager.isSpectator(online)) {
                SPECTATOR.addEntry(online.getName());

                online.setPlayerListName("§7" + online.getName());
                online.setCustomName("§7" + online.getName());
                online.setDisplayName("§7" + online.getName());
                continue;
            }

            Team scoreboardTeam = scoreboard.getTeam(team.teamId + "_" + team.teamName.toUpperCase());
            if (scoreboardTeam == null) scoreboardTeam = scoreboard.registerNewTeam(team.teamId + "_" + team.teamName.toUpperCase());
            scoreboardTeam.addEntry(online.getName());

            online.setPlayerListName(team.teamPrefix + online.getName());
            online.setCustomName(team.teamPrefix + online.getName());
            online.setDisplayName(team.teamPrefix + online.getName());
        }

        player.setScoreboard(scoreboard);

        PotterWars.getInstance().setMetadata(player, "SCOREBOARD_SIDEBAR", scoreboard);
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

    public void createTeam(Player player, String teamName, ChatColor color, String prefix, String suffix) {
        Scoreboard scoreboard = player.getScoreboard();

        if (scoreboard.getEntryTeam(player.getName()) != null)
            scoreboard.getEntryTeam(player.getName()).removeEntry(player.getName());

        Team team = scoreboard.getTeam(teamName);
        if (team == null) team = scoreboard.registerNewTeam(teamName);

        team.setPrefix(prefix);
        team.setColor(color);
        team.setSuffix(suffix);

        team.addEntry(player.getName());
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