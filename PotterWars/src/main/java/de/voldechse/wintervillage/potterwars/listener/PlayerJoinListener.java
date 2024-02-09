package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void execute(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();
        PotterWars.getInstance().gameManager.clearPlayer(player, true);

        if (player.hasMetadata("PETRIFICUS_TOTALUS"))
            PotterWars.getInstance().removeMetadata(player, "PETRIFICUS_TOTALUS");
        if (player.hasMetadata("BUILD_MODE")) PotterWars.getInstance().removeMetadata(player, "BUILD_MODE");
        if (player.hasMetadata("EDIT_TEAM")) PotterWars.getInstance().removeMetadata(player, "EDIT_TEAM");

        Types gamePhase = PotterWars.getInstance().gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase != Types.LOBBY) {
            PotterWars.getInstance().scoreboardManager.generateScoreboard(player);
            PotterWars.getInstance().gameManager.setSpectator(player, true);
            return;
        }

        Bukkit.broadcastMessage("§8» §f" + player.getName() + " §7hat das Spiel betreten");

        player.teleport(getLobbyLocation());
        player.getInventory().setItem(0, new ItemBuilder(Material.CHEST, 1, "§cWähle dein Kit").build());
        player.getInventory().setItem(1, new ItemBuilder(Material.NETHER_STAR, 1, "§aTeamauswahl").build());

        Countdown countdown = PotterWars.getInstance().gameStateManager.currentGameState().getCountdown();
        boolean enoughPlayers = PotterWars.getInstance().gameManager.getLivingPlayers().size() >= PotterWars.getInstance().minPlayers;
        boolean allowedToStart = countdown.isSleeping() && enoughPlayers;

        if (!allowedToStart) return;
        if (PotterWars.getInstance().PAUSED) return;

        countdown.stopCountdown(false);
        countdown.setInitializedTime(PotterWars.getInstance().lobbyCountdown);
        countdown.startCountdown(PotterWars.getInstance().lobbyCountdown, false);
    }

    public Location getLobbyLocation() {
        Document document = PotterWars.getInstance().configDocument.getDocument("lobbySpawn");
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