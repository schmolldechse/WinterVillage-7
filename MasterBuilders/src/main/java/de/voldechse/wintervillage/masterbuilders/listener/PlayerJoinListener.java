package de.voldechse.wintervillage.masterbuilders.listener;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.gamestate.Types;
import de.voldechse.wintervillage.masterbuilders.gamestate.list.GameStateVoteBuildings;
import de.voldechse.wintervillage.masterbuilders.teams.Team;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final MasterBuilders plugin = InjectionLayer.ext().instance(MasterBuilders.class);

    @EventHandler
    public void execute(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.plugin.gameManager.clearPlayer(player, true);

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();

        if (gamePhase != Types.LOBBY) {
            event.setJoinMessage(null);

            if (player.hasMetadata("REJOIN_TEAM")
                    && (gamePhase == Types.VOTING_THEME
                    || gamePhase == Types.BUILDING_PHASE
                    || gamePhase == Types.VOTING_BUILDINGS)) {

                Team team = this.plugin.teamManager.getTeam(player.getMetadata("REJOIN_TEAM").get(0).asInt());
                if (team == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cDein Team wurde entfernt, da es keine Spieler mehr gab");
                    this.plugin.gameManager.setSpectator(player, true);
                    this.plugin.removeMetadata(player, "REJOIN_TEAM");
                    return;
                }
                this.plugin.teamManager.setCurrentTeam(player, -1, team.teamId);
                this.plugin.removeMetadata(player, "REJOIN_TEAM");

                Location teamLocation = new Location(
                        Bukkit.getWorld(team.playerSpawn.getWorld()),
                        team.playerSpawn.getX(),
                        team.playerSpawn.getY(),
                        team.playerSpawn.getZ(),
                        team.playerSpawn.getYaw(),
                        team.playerSpawn.getPitch()
                );

                player.setGameMode(GameMode.CREATIVE);

                switch (gamePhase) {
                    case VOTING_THEME, BUILDING_PHASE -> {
                        player.teleport(teamLocation);
                        player.getInventory().setItem(8, new ItemBuilder(Material.BARRIER, 1, "§4ZURÜCKSETZEN").build());
                        player.sendMessage(this.plugin.serverPrefix + "§eDu wurdest zurück zu deinem Plot teleportiert!");
                    }

                    case VOTING_BUILDINGS -> {
                        Team currentTeam = this.plugin.teamManager.getTeamList().get(GameStateVoteBuildings.currentIndex);

                        GameStateVoteBuildings state = InjectionLayer.ext().instance(GameStateVoteBuildings.class);
                        state.displayPlot(currentTeam);
                        state.giveRateItems(currentTeam);
                    }
                }

                this.plugin.scoreboardManager.generateScoreboard(player);
                this.plugin.scoreboardManager.playerList();

                this.plugin.getInstance().getLogger().info(player.getName() + " rejoined the game");
                return;
            }

            this.plugin.gameManager.setSpectator(player, true);
            this.plugin.scoreboardManager.playerList();
            return;
        }

        if (player.hasMetadata("ALREADY_VOTED_THEME"))
            this.plugin.removeMetadata(player, "ALREADY_VOTED_THEME");
        if (player.hasMetadata("SCOREBOARD_SIDEBAR"))
            this.plugin.removeMetadata(player, "SCOREBOARD_SIDEBAR");
        if (player.hasMetadata("RESET_BUILDING")) this.plugin.removeMetadata(player, "RESET_BUILDING");
        if (player.hasMetadata("BUILD_MODE")) this.plugin.removeMetadata(player, "BUILD_MODE");
        if (player.hasMetadata("EDIT_TEAM")) this.plugin.removeMetadata(player, "EDIT_TEAM");

        if (this.plugin.maxPlayersInTeam >= 2)
            player.getInventory().setItem(0, new ItemBuilder(Material.NETHER_STAR, 1, "§aTeamauswahl").build());

        /**
         * CLOUD IMPLEMENTATION
         */
        if (!plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join()) return;

        PermissionUser permissionUser = plugin.permissionManagement.userAsync(player.getUniqueId()).join();
        if (permissionUser == null) return;
        PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) return;

        event.setJoinMessage("§a» " + permissionGroup.color() + permissionUser.name() + " §fhat das Spiel betreten");

        this.plugin.scoreboardManager.playerList();

        player.teleport(getLobbyLocation());

        Countdown countdown = this.plugin.gameStateManager.currentGameState().getCountdown();
        boolean enoughPlayers = this.plugin.gameManager.getPlayers_start().size() >= this.plugin.minPlayers;
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