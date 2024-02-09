package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import de.voldechse.wintervillage.potterwars.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler
    public void execute(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        player.getScoreboard().getTeams().forEach(org.bukkit.scoreboard.Team::unregister);

        Types gamePhase = PotterWars.getInstance().gameStateManager.currentGameState().getGameStatePhase();

        Team team = null;
        if (PotterWars.getInstance().teamManager.isPlayerInTeam(player))
            team = PotterWars.getInstance().teamManager.getTeam(player);

        switch (gamePhase) {
            case LOBBY -> {
                event.setQuitMessage("§8« §f" + player.getName() + " §7hat das Spiel verlassen");
                if (team != null)
                    PotterWars.getInstance().teamManager.removeFromCurrentTeam(player, team.teamId);

                if (PotterWars.getInstance().gameManager.getLivingPlayers().size() - 1 <= PotterWars.getInstance().minPlayers) {
                    Countdown countdown = PotterWars.getInstance().gameStateManager.currentGameState().getCountdown();
                    countdown.stopCountdown(false);
                    countdown.sleepCountdown(15);

                    PotterWars.getInstance().STARTED = false;

                    Bukkit.getOnlinePlayers().forEach(players -> {
                        players.setExp(0.0f);
                        players.setLevel(0);
                    });

                    Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§cSpielstart abgebrochen!");
                }
            }

            case PREPARING_START, INGAME, OVERTIME -> {
                if (team == null) return;

                team = PotterWars.getInstance().teamManager.getTeam(player);

                Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + team.teamPrefix + player.getName() + " §7ist gestorben");
                PotterWars.getInstance().teamManager.removeFromCurrentTeam(player, team.getTeamId());

                if (PotterWars.getInstance().teamManager.getTeam(team.getTeamId()).players.isEmpty()) {
                    Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§7Das Team §r" + team.teamPrefix + team.teamName + " §7ist ausgeschieden");
                    PotterWars.getInstance().teamManager.removeTeam(team);
                }

                if (!PotterWars.getInstance().gameManager.checkGame())
                    Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§7Es verbleiben noch §b" + (PotterWars.getInstance().gameManager.getLivingPlayers().size() - 1) + " §7Spieler und §b" + PotterWars.getInstance().teamManager.getTeamList().size() + " §7Teams");
            }

            case RESTART -> {
                event.setQuitMessage("§8« §f" + player.getName() + " §7hat das Spiel verlassen");
            }
        }
    }
}