package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Objects;
import java.util.UUID;

public class PlayerDeathListener implements Listener {

    @EventHandler
    public void execute(PlayerDeathEvent event) {
        event.setDeathMessage(null);
        event.setDroppedExp(0);
        event.getDrops().clear();

        Player diedPlayer = event.getEntity();

        diedPlayer.getScoreboard().getTeams().forEach(org.bukkit.scoreboard.Team::unregister);

        Team team = PotterWars.getInstance().teamManager.getTeam(diedPlayer);

        PotterWars.getInstance().gameManager.addSpectator(diedPlayer);

        if (diedPlayer.getKiller() != null) {
            Player killer = diedPlayer.getKiller();
            killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            killer.sendMessage(PotterWars.getInstance().serverPrefix + "§7Du hast den Spieler §r" + team.teamPrefix + diedPlayer.getName() + " §7getötet");

            Team killerTeam = PotterWars.getInstance().teamManager.getTeam(killer);

            diedPlayer.sendMessage(PotterWars.getInstance().serverPrefix + "§cDu wurdest von §r" + killerTeam.teamPrefix + killer.getName() + " §cgetötet");
            killer.sendMessage(PotterWars.getInstance().serverPrefix + "§7Du hast den Spieler §r" + team.teamPrefix + diedPlayer.getName() + " §7getötet");

            Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + team.teamPrefix + diedPlayer.getName() + " §7wurde von §r" + killerTeam.teamPrefix + killer.getName() + " §7getötet");
        } else {
            if (diedPlayer.hasMetadata("LAST_DAMAGER")) {
                UUID lastDamagerUUID = Objects.requireNonNull((UUID) diedPlayer.getMetadata("LAST_DAMAGER").get(0).value());
                if (Bukkit.getPlayer(lastDamagerUUID) != null && PotterWars.getInstance().teamManager.isPlayerInTeam(Bukkit.getPlayer(lastDamagerUUID))) {
                    if (!PotterWars.getInstance().teamManager.isPlayerInTeam(Bukkit.getPlayer(lastDamagerUUID))) return;

                    Player killer = Bukkit.getPlayer(lastDamagerUUID);
                    killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    killer.sendMessage(PotterWars.getInstance().serverPrefix + "§7Du hast den Spieler §r" + team.teamPrefix + diedPlayer.getName() + " §7getötet");

                    Team killerTeam = PotterWars.getInstance().teamManager.getTeam(killer);
                    diedPlayer.sendMessage(PotterWars.getInstance().serverPrefix + "§cDu wurdest von §r" + killerTeam.teamPrefix + killer.getName() + " §cgetötet");
                    Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + team.teamPrefix + diedPlayer.getName() + " §7wurde von §r" + killerTeam.teamPrefix + killer.getName() + " §7getötet");

                    PotterWars.getInstance().removeMetadata(diedPlayer, "LAST_DAMAGER");
                }
            } else {
                Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + team.teamPrefix + diedPlayer.getName() + " §7ist gestorben");
            }
        }

        PotterWars.getInstance().teamManager.removeFromCurrentTeam(diedPlayer, team.teamId);
        if (team.players.isEmpty()) {
            Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§7Das Team §r" + team.teamPrefix + team.teamName + " §7ist ausgeschieden");
            PotterWars.getInstance().teamManager.removeTeam(team);
        }

        if (!PotterWars.getInstance().gameManager.checkGame()) {
            Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§7Es verbleiben noch §b" + PotterWars.getInstance().gameManager.getLivingPlayers().size() + " §7Spieler und §b" + PotterWars.getInstance().teamManager.teamList.size() + " §7Teams");
        }


        if (diedPlayer.hasMetadata("PETRIFICUS_TOTALUS"))
            PotterWars.getInstance().removeMetadata(diedPlayer, "PETRIFICUS_TOTALUS");
    }
}