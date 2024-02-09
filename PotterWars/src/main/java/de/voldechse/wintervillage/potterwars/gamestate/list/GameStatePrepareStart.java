package de.voldechse.wintervillage.potterwars.gamestate.list;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.countdown.CountdownListener;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.GameState;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import de.voldechse.wintervillage.potterwars.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class GameStatePrepareStart extends GameState {

    private Countdown countdown;

    @Override
    public void startCountdown() {
        this.countdown = new Countdown(PotterWars.getInstance(), new CountdownListener() {
            @Override
            public void start() {
                PotterWars.getInstance().teamManager.setPlayersInTeam();
                teleportPlayers();

                PotterWars.getInstance().gameManager.clearPlayer(true);
                PotterWars.getInstance().gameManager.playSound(Sound.BLOCK_ANVIL_LAND, 1.0f);

                PotterWars.getInstance().gameManager.getLivingPlayers().forEach(player -> {
                    player.getInventory().setItem(0, new ItemBuilder(Material.STICK, 1, "§cZauberstab").build());
                    PotterWars.getInstance().kitManager.giveItems(player);

                    player.setHealthScale(40D);
                    player.setMaxHealth(40D);
                    player.setHealth(40D);

                    player.setLevel(150);
                });

                Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§eDie Spieler werden auf ihre Inseln teleportiert...");

                PotterWars.getInstance().PLAYING = PotterWars.getInstance().gameManager.getLivingPlayers().size();

                PotterWars.getInstance().scoreboardManager.generateScoreboard();
                PotterWars.getInstance().scoreboardManager.updateScoreboard("currentPlayers", " §a" + PotterWars.getInstance().PLAYING + "§8/§a" + PotterWars.getInstance().gameManager.getLivingPlayers().size(), "");
            }

            @Override
            public void stop() {
                endCountdown();
            }

            @Override
            public void second(int i) {
                switch (i) {
                    case 10, 5 -> {
                        Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§7Das Spiel beginnt in §e" + i + " §7Sekunden");
                        PotterWars.getInstance().gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }

                    case 3 -> {
                        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("§c§l" + i, ""));
                        PotterWars.getInstance().gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }
                    case 2 -> {
                        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("§e§l" + i, ""));
                        PotterWars.getInstance().gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }
                    case 1 -> {
                        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle("§a§l" + i, ""));
                        PotterWars.getInstance().gameManager.playSound(Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F);
                    }
                }
            }

            @Override
            public void sleep() {

            }
        });
        this.countdown.startCountdown(PotterWars.getInstance().preparingStartCountdown, false);
    }

    @Override
    public void endCountdown() {
        PotterWars.getInstance().gameStateManager.nextGameState();
    }

    @Override
    public Types getGameStatePhase() {
        return Types.PREPARING_START;
    }

    @Override
    public Countdown getCountdown() {
        return this.countdown;
    }

    private void teleportPlayers() {
        for (Player player : PotterWars.getInstance().gameManager.getLivingPlayers()) {
            Team team = PotterWars.getInstance().teamManager.getTeam(player);

            String worldName = team.teamPosition.getWorld();
            double positionX = team.teamPosition.getX();
            double positionY = team.teamPosition.getY();
            double positionZ = team.teamPosition.getZ();

            float positionYaw = team.teamPosition.getYaw();
            float positionPitch = team.teamPosition.getPitch();

            Location location = new Location(Bukkit.getWorld(worldName), positionX, positionY, positionZ, positionYaw, positionPitch);
            player.teleport(location);
        }
    }
}