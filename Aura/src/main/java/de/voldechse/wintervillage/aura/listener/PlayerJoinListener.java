package de.voldechse.wintervillage.aura.listener;

import de.voldechse.wintervillage.aura.Aura;
import de.voldechse.wintervillage.aura.gamestate.Types;
import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.document.Document;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final Aura plugin = InjectionLayer.ext().instance(Aura.class);

    @EventHandler
    public void execute(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.plugin.gameManager.clearPlayer(player, true);

        if (player.hasMetadata("BUILD_MODE")) this.plugin.removeMetadata(player, "BUILD_MODE");

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase != Types.LOBBY) {
            event.setJoinMessage(null);

            this.plugin.scoreboardManager.generateScoreboard(player);
            this.plugin.gameManager.setSpectator(player, true);
            return;
        }

        if (!plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join()) return;

        PermissionUser permissionUser = plugin.permissionManagement.userAsync(player.getUniqueId()).join();
        if (permissionUser == null) return;
        PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) return;

        event.setJoinMessage("§a» " + permissionGroup.color() + permissionUser.name() + " §fhat das Spiel betreten");

        this.plugin.scoreboardManager.playerList();

        player.teleport(getLobbyLocation());

        Countdown countdown = this.plugin.gameStateManager.currentGameState().getCountdown();
        boolean enoughPlayers = this.plugin.gameManager.getLivingPlayers().size() >= this.plugin.minPlayers;
        boolean allowedToStart = countdown.isSleeping() && enoughPlayers;

        if (!allowedToStart) return;
        if (this.plugin.PAUSED) return;

        countdown.stopCountdown(false);
        countdown.setInitializedTime(this.plugin.lobbyCountdown);
        countdown.startCountdown(this.plugin.lobbyCountdown, false);
    }

    public Location getLobbyLocation() {
        Document document = this.plugin.configDocument.getDocument("lobbySpawn");
        return new Location(
                Bukkit.getWorld(document.getString("world")),
                document.getDouble("x"),
                document.getDouble("y"),
                document.getDouble("z"),
                document.getFloat("yaw"),
                document.getFloat("pitch")
        );
    }
}