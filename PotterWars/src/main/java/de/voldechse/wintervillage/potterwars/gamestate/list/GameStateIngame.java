package de.voldechse.wintervillage.potterwars.gamestate.list;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.countdown.CountdownListener;
import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.GameState;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import de.voldechse.wintervillage.potterwars.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class GameStateIngame extends GameState {
    public static int levelThread;

    private Countdown countdown;

    @Override
    public void startCountdown() {
        int pvpEnablingAfter = PotterWars.getInstance().ingameCountdown - PotterWars.getInstance().pvpEnabledAfter;
        int worldBorderAfter = PotterWars.getInstance().ingameCountdown - PotterWars.getInstance().worldBorderAfter;

        this.countdown = new Countdown(PotterWars.getInstance(), new CountdownListener() {
            @Override
            public void start() {
                PotterWars.getInstance().gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F);
                PotterWars.getInstance().teamManager.getTeamList().removeIf(team -> team.players.isEmpty());

                Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§eDiese Runde startete mit §b" + PotterWars.getInstance().gameManager.getLivingPlayers().size() + "§8/§b" + PotterWars.getInstance().PLAYING + " §eSpieler");

                Bukkit.broadcastMessage("§8----------- §eTEAMS §8-----------");
                for (Team team : PotterWars.getInstance().teamManager.teamList) {
                    if (team.players.isEmpty()) {
                        PotterWars.getInstance().teamManager.removeTeam(team);
                        continue;
                    }

                    StringBuilder stringBuilder = new StringBuilder();
                    for (Player players : PotterWars.getInstance().teamManager.getTeam(team.getTeamId()).players) {
                        if (!stringBuilder.isEmpty()) stringBuilder.append("§8, ");
                        stringBuilder.append(team.teamPrefix).append(players.getName());
                    }
                    Bukkit.broadcastMessage("§7Team §r" + team.teamPrefix + team.teamName + "§8: " + stringBuilder.toString());
                }
                Bukkit.broadcastMessage("§8---------------------------");

                startLevelThread();
            }

            @Override
            public void stop() {
                endCountdown();
            }

            @Override
            public void second(int var0) {
                PotterWars.getInstance().scoreboardManager.updateScoreboard("ingameTimer", " §e" + String.format("%02d:%02d", (var0 / 60), (var0 % 60)), "" + (PotterWars.getInstance().PVP_ENABLED ? "" : " §c§o(Schutzzeit)"));
                PotterWars.getInstance().scoreboardManager.updateScoreboard("currentPlayers", " §a" + PotterWars.getInstance().gameManager.getLivingPlayers().size() + "§8/§a" + PotterWars.getInstance().PLAYING, "");

                if (var0 == PotterWars.getInstance().ingameCountdown) {
                    Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§7Die Schutzzeit endet in §e" + PotterWars.getInstance().pvpEnabledAfter + " Sekunden");
                    PotterWars.getInstance().gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f);
                } else if (var0 == pvpEnablingAfter) {
                    PotterWars.getInstance().PVP_ENABLED = true;

                    Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§eDie Schutzzeit ist vorbei");
                    PotterWars.getInstance().gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f);
                }

                if (PotterWars.getInstance().USE_WORLDBORDER && var0 == worldBorderAfter)
                    PotterWars.getInstance().worldBorderController.start();

                switch (var0) {
                    case 5 * 60 -> {
                        Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§aDie Kisten wurden neu aufgefüllt");
                        PotterWars.getInstance().gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f);

                        //TODO: PotterWars.getInstance().initializeChests();
                        PotterWars.getInstance().chestManager.refillChest();
                    }

                    case 3 * 60 -> {
                        Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§cSollte es in §b" + (var0 / 60) + " Minuten §ckeinen Gewinner geben, wird das Spiel automatisch um §b" + (PotterWars.getInstance().overtimeCountdown / 60) + " Minute" + ((PotterWars.getInstance().overtimeCountdown / 60) > 1 ? "n" : "") + " §cverlängert");
                    }

                    case 60, 50, 40, 30, 20, 15, 10, 5, 4, 3, 2 -> {
                        Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§cVerlängerung in §b" + var0 + " Sekunden");
                    }

                    case 1 -> {
                        Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§cVerlängerung in §b" + var0 + " Sekunde");
                    }

                    default -> {

                    }
                }
            }

            @Override
            public void sleep() {

            }
        });

        this.countdown.startCountdown(PotterWars.getInstance().ingameCountdown, false);
    }

    @Override
    public void endCountdown() {
        if (PotterWars.getInstance().teamManager.teamList.size() > 1)
            PotterWars.getInstance().gameStateManager.nextGameState();
        else {
            PotterWars.getInstance().worldBorderController.stopTasks();

            PotterWars.getInstance().gameManager.playSound(Sound.ENTITY_WITHER_DEATH, 1.0F);
            Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§cDas Spiel wurde beendet, da es keinen Gewinner gab");
            Bukkit.getScheduler().cancelTask(GameStateIngame.this.levelThread);

            PotterWars.getInstance().gameStateManager.lastGameState();
        }
    }

    @Override
    public Types getGameStatePhase() {
        return Types.INGAME;
    }

    @Override
    public Countdown getCountdown() {
        return this.countdown;
    }

    private void startLevelThread() {
        int levelMultiplier = PotterWars.getInstance().levelMultiplier;
        if (levelMultiplier > 1.0)
            Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§eIn dieser Runde ist der Levelmultiplier auf §cX" + levelMultiplier);

        this.levelThread = Bukkit.getScheduler().scheduleSyncRepeatingTask(PotterWars.getInstance(), () -> PotterWars.getInstance().gameManager.getLivingPlayers().forEach(player -> player.setLevel(player.getLevel() + 7 * levelMultiplier)), 0L, 7L);
    }
}