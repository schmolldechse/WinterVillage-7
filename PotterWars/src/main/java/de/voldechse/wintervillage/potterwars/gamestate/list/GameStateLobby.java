package de.voldechse.wintervillage.potterwars.gamestate.list;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.countdown.CountdownListener;
import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.GameState;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

public class GameStateLobby extends GameState {

    private Countdown countdown;

    @Override
    public void startCountdown() {
        this.countdown = new Countdown(PotterWars.getInstance(), new CountdownListener() {
            @Override
            public void start() {
            }

            @Override
            public void stop() {
                endCountdown();
            }

            @Override
            public void second(int i) {
                Bukkit.getOnlinePlayers().forEach(allPlayers -> {
                    allPlayers.setExp((float) i / countdown.getInitializedTime());
                    allPlayers.setLevel(i);
                });
                switch (i) {
                    case 60, 50, 40, 30, 20, 15, 10 -> {
                        Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§7Die Runde startet in §e" + i + " §7Sekunden");
                        PotterWars.getInstance().gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }

                    case 5 -> {
                        Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§7Gespielt wird die Karte §c" + PotterWars.getInstance().gameData.getMapName() + " §7von§8: §c" + PotterWars.getInstance().gameData.getMapBuilder());
                        Bukkit.getOnlinePlayers().forEach(allPlayers -> {
                            allPlayers.sendTitle("§7Rundenstart in...", "§b§l5 §7Sekunden");
                        });
                        Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§7Die Runde startet in §e" + i + " §7Sekunden");
                        PotterWars.getInstance().gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }

                    case 4, 3, 2 -> {
                        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("§7Rundenstart in...", "§b§l" + i + " §7Sekunden"));
                        PotterWars.getInstance().gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }

                    case 1 -> {
                        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("§7Rundenstart in...", "§b§l" + i + " §7Sekunde"));
                        PotterWars.getInstance().gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }

                    default -> {
                    }
                }
            }

            @Override
            public void sleep() {
                if (PotterWars.getInstance().PAUSED) {
                    Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cSpielstart pausiert")));
                    return;
                }
                int missingPlayers = PotterWars.getInstance().minPlayers - PotterWars.getInstance().gameManager.getLivingPlayers().size();
                Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cEs wird noch auf §b" + missingPlayers + " §cSpieler gewartet!")));
            }
        });
        this.countdown.sleepCountdown(PotterWars.getInstance().lobby_sleepDelay);
    }

    @Override
    public void endCountdown() {
        PotterWars.getInstance().gameStateManager.nextGameState();
    }

    @Override
    public Types getGameStatePhase() {
        return Types.LOBBY;
    }

    @Override
    public Countdown getCountdown() {
        return this.countdown;
    }
}