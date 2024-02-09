package de.voldechse.wintervillage.aura.utils;

import de.voldechse.wintervillage.aura.Aura;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
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
    
    private final Aura plugin = InjectionLayer.ext().instance(Aura.class);

    public boolean isSpectator(Player player) {
        return this.plugin.SPECTATORS.contains(player.getUniqueId());
    }

    public void addSpectator(Player player) {
        if (isSpectator(player)) return;
        this.plugin.SPECTATORS.add(player.getUniqueId());
    }

    public void removeSpectator(Player player) {
        if (!isSpectator(player)) return;
        this.plugin.SPECTATORS.remove(player);
    }

    public void setSpectator(Player player, boolean locationSpawn) {
        player.sendMessage(this.plugin.serverPrefix + "§eDu beobachtest nun!");
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

        this.plugin.scoreboardManager.generateScoreboard(player);
    }

    public Inventory getSpectatorInventory() {
        int inventorySize = (this.getLivingPlayers().size() / 9 + 1) * 9;
        Inventory inventory = Bukkit.createInventory(null, inventorySize, "§bTeleporter");
        this.getLivingPlayers().forEach(player -> {
            ItemStack itemStack = new ItemBuilder(Material.PLAYER_HEAD, 1, player.getName()).build();
            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

            PermissionUser permissionUser = plugin.permissionManagement.userAsync(player.getUniqueId()).join();
            if (permissionUser == null) skullMeta.setDisplayName("§f" + player.getName());
            PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
            if (permissionGroup == null) skullMeta.setDisplayName("§f" + player.getName());

            if (permissionUser != null && permissionGroup != null) skullMeta.setDisplayName(permissionGroup.color() + permissionUser.name());

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
                players.hidePlayer(this.plugin.getInstance(), player);
            } else {
                player.showPlayer(this.plugin.getInstance(), players);
                players.showPlayer(this.plugin.getInstance(), player);
            }
        });
    }

    public boolean checkGame() {
        if (getLivingPlayers().size() == 1) {
            Player winner = getLivingPlayers().get(0);

            PermissionUser permissionUser = this.plugin.permissionManagement.getOrCreateUserAsync(winner.getUniqueId(), winner.getName()).join();
            if (permissionUser == null) return true;
            PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
            if (permissionGroup == null) return true;

            winner.sendMessage(this.plugin.serverPrefix + "§aDu hast gewonnen!");
            Bukkit.broadcastMessage(this.plugin.serverPrefix + permissionGroup.color() + permissionUser.name() + " §chat §cgewonnen");

            this.plugin.gameStateManager.currentGameState().getCountdown().stopCountdown(false);
            this.plugin.gameStateManager.lastGameState();

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
        this.plugin.SPECTATORS.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            a.add(player);
        });
        return a;
    }
}