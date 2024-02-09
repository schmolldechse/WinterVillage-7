package de.voldechse.wintervillage.aura.gamestate.list;

import de.voldechse.wintervillage.aura.Aura;
import de.voldechse.wintervillage.aura.gamestate.GameState;
import de.voldechse.wintervillage.aura.gamestate.Types;
import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.countdown.CountdownListener;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

public class GameStateLobby extends GameState {

    private final Aura plugin = InjectionLayer.ext().instance(Aura.class);

    private Countdown countdown;

    private final Component actionbar = Component.text("Spielstart pausiert")
            .color(NamedTextColor.RED);

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
                    Bukkit.getOnlinePlayers().forEach(player -> player.sendActionBar(actionbar));
                    return;
                }

                int missingPlayers = plugin.minPlayers - plugin.gameManager.getLivingPlayers().size();
                Bukkit.broadcast(Component.text(plugin.serverPrefix));

                //plugin.serverPrefix + "§cEs wird noch auf §b" + missingPlayers + " §cgewartet"
                return;
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