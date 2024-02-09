package de.voldechse.wintervillage.ttt.gamestate.list;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.countdown.CountdownListener;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.GameState;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;

public class GameStateRestart extends GameState {
    
    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    public static final int RESTART_TIME = 15;

    private Countdown countdown;

    @Override
    public void startCountdown() {
        this.countdown = new Countdown(this.plugin.getInstance(), new CountdownListener() {
            @Override
            public void start() {

            }

            @Override
            public void stop() {
                Bukkit.broadcastMessage(plugin.serverPrefix + "§cDer Server startet nun neu");
                endCountdown();
            }

            @Override
            public void second(int i) {
                switch (i) {
                    case 10, 5, 3, 2 ->
                            Bukkit.broadcastMessage(plugin.serverPrefix + "§cDer Server startet in §b" + i + " §cSekunden neu");
                    case 1 ->
                            Bukkit.broadcastMessage(plugin.serverPrefix + "§cDer Server startet in §b" + i + " §cSekunde neu");
                    default -> {
                    }
                }
            }

            @Override
            public void sleep() {
            }
        });
        this.countdown.startCountdown(GameStateRestart.RESTART_TIME, false);
    }

    @Override
    public void endCountdown() {
        Bukkit.getScheduler().runTaskLater(this.plugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                Bukkit.getServer().shutdown();
            }
        }, 40L);
    }

    @Override
    public Types getGameStatePhase() {
        return Types.RESTART;
    }

    @Override
    public Countdown getCountdown() {
        return this.countdown;
    }
}