package de.voldechse.wintervillage.masterbuilders.gamestate.list;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.countdown.CountdownListener;
import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.gamestate.GameState;
import de.voldechse.wintervillage.masterbuilders.gamestate.Types;
import de.voldechse.wintervillage.masterbuilders.teams.Team;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.List;

import static de.voldechse.wintervillage.masterbuilders.gamestate.list.GameStateVoteThemes.winningTheme;

public class GameStateIngame extends GameState {
    
    private final MasterBuilders plugin = InjectionLayer.ext().instance(MasterBuilders.class);

    private Countdown countdown;

    private List<Villager> addedVillagers = new ArrayList<Villager>();

    @Override
    public void startCountdown() {
        this.countdown = new Countdown(this.plugin.getInstance(), new CountdownListener() {
            @Override
            public void start() {
                if (plugin.SPAWN_VILLAGERS) {
                    for (Team team : plugin.teamManager.getTeamList()) {
                        Location villagerSpawn = new Location(
                                Bukkit.getWorld(team.villagerSpawn.getWorld()),
                                team.villagerSpawn.getX(),
                                team.villagerSpawn.getY(),
                                team.villagerSpawn.getZ(),
                                team.villagerSpawn.getYaw(),
                                team.villagerSpawn.getPitch()
                        );
                        Villager villager = villagerSpawn.getWorld().spawn(villagerSpawn, Villager.class);
                        villager.setProfession(Villager.Profession.NITWIT);
                        villager.setCustomName("§6Rechtsklick mich");
                        villager.setCustomNameVisible(true);
                        villager.setAI(false);

                        addedVillagers.add(villager);
                    }
                }

                plugin.scoreboardManager.generateScoreboard();
                plugin.scoreboardManager.updateScoreboard("currentTheme", " §c" + winningTheme, "");
                Bukkit.broadcastMessage(plugin.serverPrefix + "§eDiese Runde startete mit §b" + plugin.teamManager.getPlayerTeamList().size() + "§8/§b" + plugin.PLAYING + " §eSpieler");
                Bukkit.broadcastMessage(plugin.serverPrefix + "§eSollte das Fliegen nicht mehr möglich sein, nutze §c/fix");
            }

            @Override
            public void stop() {
                addedVillagers.forEach(villager -> {
                    if (villager != null) {
                        villager.setHealth(0.0D);
                        villager.remove();
                    }
                });

                plugin.teamManager.getPlayerTeamList().forEach(player -> {
                    plugin.gameManager.clearPlayer(player, true);
                    player.setAllowFlight(true);
                    player.setFlying(true);
                });

                Bukkit.getLogger().info("Ermittle Teams, dessen Spieler das Spiel verlassen haben...");
                plugin.teamManager.getTeamList().removeIf(team -> team.plotOwner.isEmpty());

                endCountdown();
            }

            @Override
            public void second(int i) {
                Bukkit.getOnlinePlayers().forEach(player -> plugin.scoreboardManager.updateScoreboard(player, "ingameTimer", " §e" + String.format("%02d:%02d", (i / 60), (i % 60)), ""));
                switch (i) {
                    case 5 * 60 -> Bukkit.broadcastMessage(plugin.serverPrefix + "§eDie Bauphase endet in §c" + (i / 60) + " §eMinuten!");
                    case 60, 30 -> {
                        Bukkit.broadcastMessage(plugin.serverPrefix + "§eDie Bauphase endet in §c" + i + " §eSekunden!");
                        plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }
                    case 10 -> plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    case 3, 2, 1 -> {
                        plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                        plugin.gameManager.broadcastTitle("§c" + i, "");
                    }
                    default -> {
                    }
                }
            }

            @Override
            public void sleep() {
            }
        });
        this.countdown.startCountdown(this.plugin.buildingCountdown, false);
    }

    @Override
    public void endCountdown() {
        this.plugin.gameStateManager.nextGameState();
    }

    @Override
    public Types getGameStatePhase() {
        return Types.BUILDING_PHASE;
    }

    @Override
    public Countdown getCountdown() {
        return this.countdown;
    }
}