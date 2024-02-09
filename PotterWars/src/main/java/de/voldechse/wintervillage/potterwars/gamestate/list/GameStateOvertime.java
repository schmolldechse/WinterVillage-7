package de.voldechse.wintervillage.potterwars.gamestate.list;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.countdown.CountdownListener;
import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.GameState;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import org.bukkit.Bukkit;
import org.bukkit.Sound;

public class GameStateOvertime extends GameState {

    private Countdown countdown;

    @Override
    public void startCountdown() {
        this.countdown = new Countdown(PotterWars.getInstance(), new CountdownListener() {
            @Override
            public void start() {
                Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§c§lVERLÄNGERUNG! §cSollte es nach 5 Minuten keinen Gewinner geben, endet das Spiel");
            }

            @Override
            public void stop() {
                PotterWars.getInstance().gameManager.playSound(Sound.ENTITY_WITHER_DEATH, 1.0F);
                Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§cDas Spiel wurde beendet, da es keinen Gewinner gab");
                Bukkit.getScheduler().cancelTask(GameStateIngame.levelThread);

                endCountdown();
            }

            @Override
            public void second(int var0) {
                PotterWars.getInstance().scoreboardManager.updateScoreboard("ingameTimer", " §e+" + String.format("%02d:%02d", (var0 / 60), (var0 % 60)), " §c§oOVERTIME");
            }

            @Override
            public void sleep() {

            }
        });

        this.countdown.startCountdown(PotterWars.getInstance().overtimeCountdown, true);
    }

    @Override
    public void endCountdown() {
        PotterWars.getInstance().worldBorderController.stopTasks();
        PotterWars.getInstance().gameStateManager.nextGameState();
    }

    @Override
    public Types getGameStatePhase() {
        return Types.INGAME;
    }

    @Override
    public Countdown getCountdown() {
        return this.countdown;
    }
}