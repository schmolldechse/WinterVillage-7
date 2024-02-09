package de.voldechse.wintervillage.potterwars.gamestate.list;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.countdown.CountdownListener;
import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.GameState;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import org.bukkit.Bukkit;

public class GameStateRestart extends GameState {

    private Countdown countdown;

    @Override
    public void startCountdown() {
        this.countdown = new Countdown(PotterWars.getInstance(), new CountdownListener() {
            @Override
            public void start() {
                PotterWars.getInstance().worldBorderController.stopTasks();
            }

            @Override
            public void stop() {
                Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§cDer Server startet nun neu");
                endCountdown();
            }

            @Override
            public void second(int var0) {
                switch (var0) {
                    case 15:
                    case 10:
                    case 5:
                    case 4:
                    case 3:
                    case 2:
                        Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§cDer Server startet in §b" + var0 + " §cSekunden neu");
                        break;
                    case 1:
                        Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§cDer Server startet in §b" + var0 + " §cSekunde neu");
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void sleep() {
            }
        });
        this.countdown.startCountdown(20, false);
    }

    @Override
    public void endCountdown() {
        Bukkit.getScheduler().runTaskLater(PotterWars.getInstance(), new Runnable() {
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