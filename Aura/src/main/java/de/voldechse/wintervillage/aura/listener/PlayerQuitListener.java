package de.voldechse.wintervillage.aura.listener;

import de.voldechse.wintervillage.aura.Aura;
import de.voldechse.wintervillage.aura.gamestate.Types;
import de.voldechse.wintervillage.library.countdown.Countdown;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final Aura plugin = InjectionLayer.ext().instance(Aura.class);

    @EventHandler
    public void execute(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        player.getScoreboard().getTeams().forEach(org.bukkit.scoreboard.Team::unregister);

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();

        if (!plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join()) return;

        PermissionUser permissionUser = plugin.permissionManagement.userAsync(player.getUniqueId()).join();
        if (permissionUser == null) return;
        PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) return;

        switch (gamePhase) {
            case LOBBY -> {
                event.setQuitMessage("§c« " + permissionGroup.color() + permissionUser.name() + " §7hat das Spiel verlassen");

                if (this.plugin.gameManager.getLivingPlayers().size() - 1 <= this.plugin.minPlayers) {
                    Countdown countdown = this.plugin.gameStateManager.currentGameState().getCountdown();
                    countdown.stopCountdown(false);
                    countdown.sleepCountdown(15);

                    this.plugin.STARTED = false;

                    Bukkit.getOnlinePlayers().forEach(players -> {
                        players.setExp(0.0f);
                        players.setLevel(0);
                    });

                    Bukkit.broadcast(Component.text(this.plugin.serverPrefix)
                            .append(Component.text("Spielstart abgebrochen", NamedTextColor.RED)));
                }
            }

            case PREPARING_START, INGAME -> {
                event.setQuitMessage(null);
                if (this.plugin.gameManager.isSpectator(player)) return;

                Bukkit.broadcastMessage(this.plugin.serverPrefix + permissionGroup.color() + permissionUser.name() + " §7ist gestorben");

                if (this.plugin.gameManager.checkGame()) Bukkit.broadcastMessage(this.plugin.serverPrefix + "§fEs verbleiben noch §e" + this.plugin.gameManager.getLivingPlayers().size() + " §fSpieler");
            }

            case RESTART -> {
                event.setQuitMessage("§c« " + permissionGroup.color() + permissionUser.name() + " §7hat das Spiel verlassen");
            }
        }
    }
}