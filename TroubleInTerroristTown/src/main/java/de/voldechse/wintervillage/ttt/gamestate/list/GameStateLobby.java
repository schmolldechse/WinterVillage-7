package de.voldechse.wintervillage.ttt.gamestate.list;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.countdown.CountdownListener;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.GameState;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

public class GameStateLobby extends GameState {
    
    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    private Countdown countdown;

    @Override
    public void startCountdown() {
        this.countdown = new Countdown(this.plugin.getInstance(), new CountdownListener() {
            @Override
            public void start() {
            }

            @Override
            public void stop() {
                endCountdown();
            }

            @Override
            public void second(int v0) {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.setExp((float) v0 / countdown.getInitializedTime());
                    player.setLevel(v0);
                });

                switch (v0) {
                    case 60, 50, 40, 30, 20, 15, 10 -> {
                        Bukkit.broadcastMessage(plugin.serverPrefix + "§7Die Runde startet in §e" + v0 + " §7Sekunden");
                        plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }
                    case 5, 4, 3, 2 -> {
                        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("§7Rundenstart in...", "§b§l" + v0 + " §7Sekunden"));
                        plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }
                    case 1 -> {
                        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("§7Rundenstart in...", "§b§l" + v0 + " §7Sekunde"));
                        plugin.gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }
                    default -> {
                    }
                }
            }

            @Override
            public void sleep() {
                if (plugin.PAUSED) {
                    Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cSpielstart pausiert")));
                    return;
                }
                int missingPlayers = plugin.minPlayers - plugin.roleManager.getPlayerList().size();
                Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText("§cEs wird noch auf §b" + missingPlayers + " §cSpieler gewartet!")));
            }
        });
        this.countdown.sleepCountdown(this.plugin.lobby_sleepDelay);
    }

    @Override
    public void endCountdown() {
        this.plugin.gameStateManager.nextGameState();
    }

    @Override
    public Types getGameStatePhase() {
        return Types.LOBBY;
    }

    @Override
    public Countdown getCountdown() {
        return countdown;
    }
}