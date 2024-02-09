package de.voldechse.wintervillage.ttt.gamestate.list;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.countdown.CountdownListener;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.GameState;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.utils.position.PositionEntity;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

public class GameStateProtectionTime extends GameState {
    
    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    private Countdown countdown;

    @Override
    public void startCountdown() {
        this.countdown = new Countdown(this.plugin.getInstance(), new CountdownListener() {
            @Override
            public void start() {
                teleportPlayers();

                plugin.PLAYING = plugin.roleManager.getPlayerList().size();
                plugin.scoreboardManager.generateScoreboard();
                plugin.scoreboardManager.updateScoreboard("currentPlayers", " §a" + plugin.roleManager.getPlayerList().size() + "§8/§a" + plugin.PLAYING, "§a");
            }

            @Override
            public void stop() {
                endCountdown();
            }

            @Override
            public void second(int i) {
                plugin.scoreboardManager.updateScoreboard("currentPlayers", " §a" + plugin.roleManager.getPlayerList().size() + "§8/§a" + plugin.PLAYING, "§a");
                plugin.scoreboardManager.updateScoreboard("ingameTimer", " §e" + String.format("%02d:%02d", (i / 60), (i % 60)), "§e");

                switch (i) {
                    case 33 -> {
                        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("§c§l" + (i - 30), ""));
                        plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }
                    case 32 -> {
                        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("§e§l" + (i - 30), ""));
                        plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }
                    case 31 -> {
                        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("§a§l" + (i - 30), ""));
                        plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }
                    case 30 -> {
                        Bukkit.broadcastMessage(plugin.serverPrefix + "§eDie Runde beginnt! Sammle Waffen und rüste dich für den Kampf");
                        Bukkit.broadcastMessage(plugin.serverPrefix + "§cGrundloses Töten von Spielern ist verboten!");

                        Bukkit.broadcastMessage(plugin.serverPrefix + "§7Die Schutzzeit endet in §e" + i + " §7Sekunden");
                        plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1);
                    }
                    case 20, 10, 5, 4, 3, 2 -> {
                        Bukkit.broadcastMessage(plugin.serverPrefix + "§7Die Schutzzeit endet in §e" + i + " §7Sekunden");
                        plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }
                    case 1 -> {
                        Bukkit.broadcastMessage(plugin.serverPrefix + "§7Die Schutzzeit endet in §e" + i + " §7Sekunde");
                        plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }
                }
            }

            @Override
            public void sleep() {
            }
        });
        this.countdown.startCountdown(this.plugin.protectionCountdown, false);
    }

    private void teleportPlayers() {
        Collections.shuffle(this.plugin.positionManager.spawnPositions);
        Queue<PositionEntity> positions = new LinkedList<>(this.plugin.positionManager.spawnPositions);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.plugin.gameManager.isSpectator(player)) continue;
            if (positions.isEmpty()) {
                player.sendMessage(this.plugin.serverPrefix + "§cEs konnte kein Spawn für dich gefunden werden");
                this.plugin.gameManager.setSpectator(player, true);
                continue;
            }

            this.plugin.gameManager.clearPlayer(player, true);

            PositionEntity position = positions.poll();
            player.teleport(new Location(
                    Bukkit.getWorld(position.getWorld()),
                    position.getX(),
                    position.getY(),
                    position.getZ(),
                    position.getYaw(),
                    position.getPitch()
            ));
        }
    }

    @Override
    public void endCountdown() {
        this.plugin.gameStateManager.nextGameState();
    }

    @Override
    public Types getGameStatePhase() {
        return Types.PREPARING_START;
    }

    @Override
    public Countdown getCountdown() {
        return this.countdown;
    }
}