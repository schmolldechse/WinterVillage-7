package de.voldechse.wintervillage.masterbuilders.listener;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.gamestate.Types;
import de.voldechse.wintervillage.masterbuilders.teams.Team;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final MasterBuilders plugin = InjectionLayer.ext().instance(MasterBuilders.class);

    @EventHandler
    public void execute(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        event.setQuitMessage(null);

        if (this.plugin.gameManager.isSpectator(player)) return;

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();

        Team team = null;
        if (this.plugin.teamManager.isPlayerInTeam(player))
            team = this.plugin.teamManager.getTeam(player);

        switch (gamePhase) {
            case LOBBY -> {
                event.setQuitMessage("§c« §f" + player.getName() + " §7hat das Spiel verlassen");
                if (team != null)
                    this.plugin.teamManager.removeFromCurrentTeam(player, team.teamId);

                if (this.plugin.gameManager.getPlayers_start().size() - 1 <= this.plugin.minPlayers) {
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

            case VOTING_THEME, BUILDING_PHASE, VOTING_BUILDINGS -> {
                this.plugin.teamManager.removeFromCurrentTeam(player, team.teamId);
                this.plugin.setMetadata(player, "REJOIN_TEAM", team.teamId);
            }

            case RESTART -> {
                event.setQuitMessage("§c« §f" + player.getName() + " §7hat das Spiel verlassen");
            }
        }
    }
}