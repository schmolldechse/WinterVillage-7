package de.voldechse.wintervillage.masterbuilders.gamestate.list;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.gamestate.GameState;
import de.voldechse.wintervillage.masterbuilders.gamestate.Types;
import de.voldechse.wintervillage.masterbuilders.listener.PlayerInteractListener;
import de.voldechse.wintervillage.masterbuilders.teams.Team;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;

public class GameStateVoteBuildings extends GameState {
    
    private final MasterBuilders plugin = InjectionLayer.ext().instance(MasterBuilders.class);

    public static int currentIndex = 0;
    private int SCOREBOARD_TIME_REMAINING;
    private BukkitRunnable voteTask;
    private BukkitRunnable scoreboardTask;

    private BossBar bossBar;

    @Override
    public void startCountdown() {
        Bukkit.broadcastMessage(this.plugin.serverPrefix + "§eDas Bewerten beginnt!");
        Bukkit.broadcastMessage(this.plugin.serverPrefix + "§eBewerte nun die verschiedenen Grundstücke mithilfe der Items in deiner Hotbar");
        Bukkit.broadcastMessage(this.plugin.serverPrefix + "§cSolltest du keine Stimme abgeben, wird automatisch mit §eOKAY §cabgestimmt!");

        currentIndex = 0;

        this.plugin.scoreboardManager.generateScoreboard();
        Collections.shuffle(this.plugin.teamManager.getTeamList());

        this.bossBar = Bukkit.getServer().createBossBar("§f§lBewerte das Grundstück", BarColor.GREEN, BarStyle.SOLID, BarFlag.DARKEN_SKY);
        this.bossBar.setProgress(1);
        Bukkit.getOnlinePlayers().forEach(player -> this.bossBar.addPlayer(player));
        this.bossBar.setVisible(true);

        voteTask = new BukkitRunnable() {
            @Override
            public void run() {
                SCOREBOARD_TIME_REMAINING = 15;
                if (currentIndex < plugin.teamManager.getTeamList().size()) {
                    Team currentPlot = plugin.teamManager.getTeamList().get(currentIndex);
                    displayPlot(currentPlot);
                    giveRateItems(currentPlot);

                    List<Player> plotOwners = currentPlot.plotOwner;
                    StringBuilder stringBuilder = new StringBuilder();
                    if (!plotOwners.isEmpty()) {
                        for (int i = 0; i < plotOwners.size(); i++) {
                            if (!plugin.permissionManagement.containsUserAsync(plotOwners.get(i).getUniqueId()).join()) continue;
                            PermissionUser permissionUser = plugin.permissionManagement.userAsync(plotOwners.get(i).getUniqueId()).join();
                            if (!plugin.permissionManagement.containsUserAsync(permissionUser.uniqueId()).join()) continue;
                            PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);

                            stringBuilder.append(permissionGroup.color()).append(permissionUser.name());

                            if (plotOwners.size() > 1 && i < plotOwners.size() - 1) stringBuilder.append("§8, ");
                        }
                    }

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            Bukkit.getOnlinePlayers().forEach(players -> {
                                if (!PlayerInteractListener.PLAYER_LAST_VOTED_DATA_PER_TEAM.containsKey(players)
                                        && !currentPlot.plotOwner.contains(players)
                                        && !plugin.gameManager.isSpectator(players))
                                    PlayerInteractListener.addVotepoints(currentPlot, 3);
                                players.sendTitle("§6" + stringBuilder.toString(), "§d" + (currentIndex + 1) + "§8/§7" + plugin.teamManager.getTeamList().size());
                            });

                            PlayerInteractListener.PLAYER_LAST_VOTED_DATA_PER_TEAM.clear();

                            currentIndex++;
                            if (currentIndex >= plugin.teamManager.getTeamList().size())
                                endCountdown();
                        }
                    }.runTaskLater(plugin.getInstance(), plugin.voteBuildingsCountdownPerPlayer * 20L);
                } else endCountdown();
            }
        };
        voteTask.runTaskTimer(this.plugin.getInstance(), 0L, (this.plugin.voteBuildingsCountdownPerPlayer + 1) * 20L);

        scoreboardTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (SCOREBOARD_TIME_REMAINING > 0) {
                    SCOREBOARD_TIME_REMAINING--;

                    Bukkit.getOnlinePlayers().forEach(player -> {
                        bossBar.setProgress((float) SCOREBOARD_TIME_REMAINING / 15);

                        player.setExp((float) SCOREBOARD_TIME_REMAINING / 15);
                        player.setLevel(SCOREBOARD_TIME_REMAINING);
                        plugin.scoreboardManager.updateScoreboard(player, "ingameTimer", " §e" + String.format("%02d:%02d", (SCOREBOARD_TIME_REMAINING / 60), (SCOREBOARD_TIME_REMAINING % 60)), "");
                    });
                }
            }
        };
        scoreboardTask.runTaskTimer(this.plugin.getInstance(), 0L, 20L);
    }

    @Override
    public void endCountdown() {
        if (voteTask != null) voteTask.cancel();
        if (scoreboardTask != null) scoreboardTask.cancel();

        this.bossBar.setVisible(false);

        this.plugin.teamManager.getPlayerTeamList().forEach(player -> {
            this.plugin.gameManager.clearPlayer(player, true);
            player.setAllowFlight(true);
            player.setFlying(true);
        });

        this.plugin.gameStateManager.nextGameState();
    }

    @Override
    public Types getGameStatePhase() {
        return Types.VOTING_BUILDINGS;
    }

    @Override
    public Countdown getCountdown() {
        return null;
    }

    public void displayPlot(Team plot) {
        Location plotLocation = new Location(
                Bukkit.getWorld(plot.playerSpawn.getWorld()),
                plot.playerSpawn.getX(),
                plot.playerSpawn.getY(),
                plot.playerSpawn.getZ(),
                plot.playerSpawn.getYaw(),
                plot.playerSpawn.getPitch()
        );
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.teleport(plotLocation.clone().add(0, 10, 0));
        });
    }

    public void giveRateItems(Team plot) {
        this.plugin.teamManager.getPlayerTeamList().forEach(player -> {
            if (!plot.plotOwner.contains(player)) {
                player.getInventory().setItem(0, new ItemBuilder(Material.RED_STAINED_GLASS_PANE, 1, "§4GRAUENVOLL").build());
                player.getInventory().setItem(1, new ItemBuilder(Material.ORANGE_STAINED_GLASS_PANE, 1, "§6SCHLECHT").build());
                player.getInventory().setItem(2, new ItemBuilder(Material.YELLOW_STAINED_GLASS_PANE, 1, "§eOKAY").build());
                player.getInventory().setItem(3, new ItemBuilder(Material.GREEN_STAINED_GLASS_PANE, 1, "§aGUT").build());
                player.getInventory().setItem(4, new ItemBuilder(Material.BLUE_STAINED_GLASS_PANE, 1, "§9SUPER").build());
                player.getInventory().setItem(5, new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE, 1, "§5PERFEKT").build());
            } else {
                for (int i = 0; i < 6; i++)
                    player.getInventory().setItem(i, new ItemBuilder(Material.BARRIER, 1, "§cDU DARFST DAS NICHT BEWERTEN").build());
            }
        });
    }
}