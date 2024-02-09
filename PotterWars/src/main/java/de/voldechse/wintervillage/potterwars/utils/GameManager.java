package de.voldechse.wintervillage.potterwars.utils;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.list.GameStateIngame;
import de.voldechse.wintervillage.potterwars.team.Team;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameManager {

    public boolean isSpectator(Player player) {
        return PotterWars.getInstance().SPECTATORS.contains(player.getUniqueId());
    }

    public void addSpectator(Player player) {
        if (isSpectator(player)) return;
        PotterWars.getInstance().SPECTATORS.add(player.getUniqueId());
    }

    public void removeSpectator(Player player) {
        if (!isSpectator(player)) return;
        PotterWars.getInstance().SPECTATORS.remove(player);
    }

    public void setSpectator(Player player, boolean locationSpawn) {
        player.sendMessage(PotterWars.getInstance().serverPrefix + "§eDu beobachtest nun!");
        addSpectator(player);

        clearPlayer(player, false);

        player.getInventory().setItem(0, new ItemBuilder(Material.COMPASS, 1, "§bTeleporter").build());
        player.setGameMode(GameMode.SURVIVAL);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFlySpeed(0.2f);
        player.setCollidable(false);

        updateVisibility(player);
        if (locationSpawn && getRandomPlayer() != null)
            player.teleport(getRandomPlayer());
    }

    public Inventory getSpectatorInventory() {
        int inventorySize = (getLivingPlayers().size() / 9 + 1) * 9;
        Inventory inventory = Bukkit.createInventory(null, inventorySize, "§bTeleporter");
        getLivingPlayers().forEach(living -> inventory.addItem(new ItemBuilder(Material.PLAYER_HEAD, 1, living.getName()).owner(living.getName()).build()));


        getLivingPlayers().forEach(player -> {
            ItemStack itemStack = new ItemBuilder(Material.PLAYER_HEAD, 1, player.getName()).build();
            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
            skullMeta.setOwnerProfile(Bukkit.createPlayerProfile(player.getName()));
            itemStack.setItemMeta(skullMeta);

            inventory.addItem(itemStack);
        });

        return inventory;
    }

    public List<Player> getLivingPlayers() {
        List<Player> playingPlayers = new ArrayList<Player>();
        Bukkit.getOnlinePlayers().forEach(allPlayers -> {
            if (!isSpectator(allPlayers))
                playingPlayers.add(allPlayers);
        });
        return playingPlayers;
    }

    public void playSound(Sound sound, float soundStrength, float pitch) {
        Bukkit.getOnlinePlayers().forEach(allPlayers -> allPlayers.playSound(allPlayers.getLocation(), sound, soundStrength, pitch));
    }

    public void playSound(Sound sound, float soundStrength) {
        playSound(sound, soundStrength, soundStrength);
    }

    public void clearPlayer(boolean gamemodeSwitch) {
        Bukkit.getOnlinePlayers().forEach(player -> clearPlayer(player, gamemodeSwitch));
    }

    public void clearPlayer(Player player, boolean gamemodeSwitch_toSurvival) {
        if (gamemodeSwitch_toSurvival) player.setGameMode(GameMode.SURVIVAL);
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
        player.getInventory().clear();
        player.getInventory().setArmorContents((ItemStack[]) null);
        player.setHealthScale(20.0);
        player.setMaxHealth(20.0);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setLevel(0);
        player.setExp(0.0f);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setCollidable(true);

        updateVisibility(player);
    }

    public void updateVisibility(Player player) {
        Bukkit.getOnlinePlayers().forEach(players -> {
            if (isSpectator(player) && !isSpectator(players)) {
                players.hidePlayer(PotterWars.getInstance(), player);
            } else {
                player.showPlayer(PotterWars.getInstance(), players);
                players.showPlayer(PotterWars.getInstance(), player);
            }
        });
    }

    public boolean checkGame() {
        if (PotterWars.getInstance().teamManager.getTeamList().size() == 1) {
            Team winnerTeam = PotterWars.getInstance().teamManager.getTeamList().get(0);

            List<Player> playersInTeam = winnerTeam.players;
            playersInTeam.forEach(player -> player.sendMessage(PotterWars.getInstance().serverPrefix + "§aDu hast gewonnen!"));
            Bukkit.broadcastMessage(PotterWars.getInstance().serverPrefix + "§cDas Team §r" + winnerTeam.teamPrefix + winnerTeam.teamName + " §chat §cgewonnen");

            PotterWars.getInstance().gameStateManager.currentGameState().getCountdown().stopCountdown(false);
            PotterWars.getInstance().gameStateManager.lastGameState();

            if (Bukkit.getScheduler().isCurrentlyRunning(GameStateIngame.levelThread)) Bukkit.getScheduler().cancelTask(GameStateIngame.levelThread);
            return true;
        }
        return false;
    }

    public Player getRandomPlayer() {
        int randomIndex = new Random().nextInt(getLivingPlayers().size());
        return getLivingPlayers().get(randomIndex);
    }

    public List<Player> getSpectatorList() {
        List<Player> a = new ArrayList<>();
        PotterWars.getInstance().SPECTATORS.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            a.add(player);
        });
        return a;
    }
}