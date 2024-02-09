package de.voldechse.wintervillage.masterbuilders.utils;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
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
    
    private final MasterBuilders plugin = InjectionLayer.ext().instance(MasterBuilders.class);

    private final List<Player> spectatorList = new ArrayList<Player>();

    public boolean isSpectator(Player player) {
        return spectatorList.contains(player);
    }

    private void addSpectator(Player player) {
        if (isSpectator(player)) return;
        spectatorList.add(player);
    }

    public void removeSpectator(Player player) {
        if (!isSpectator(player)) return;
        spectatorList.remove(player);
    }

    public void setSpectator(Player player, boolean locationSpawn) {
        player.sendMessage(this.plugin.serverPrefix + "§eDu beobachtest nun!");
        addSpectator(player);

        clearPlayer(player, false);

        player.getInventory().setItem(0, new ItemBuilder(Material.COMPASS, 1, getSpectatorCompass()).build());
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
        int inventorySize = (this.plugin.teamManager.getPlayerTeamList().size() / 9 + 1) * 9;
        Inventory inventory = Bukkit.createInventory(null, inventorySize, getSpectatorCompass());
        this.plugin.teamManager.getPlayerTeamList().forEach(building -> {
            ItemStack itemStack = new ItemBuilder(Material.PLAYER_HEAD, 1).build();
            SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();

            PermissionUser permissionUser = plugin.permissionManagement.userAsync(building.getUniqueId()).join();
            if (permissionUser == null) skullMeta.setDisplayName("§f" + building.getName());
            PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
            if (permissionGroup == null) skullMeta.setDisplayName("§f" + building.getName());

            if (permissionUser != null && permissionGroup != null) skullMeta.setDisplayName(permissionGroup.color() + permissionUser.name());

            skullMeta.setOwnerProfile(Bukkit.createPlayerProfile(building.getName()));
            itemStack.setItemMeta(skullMeta);

            inventory.addItem(itemStack);
        });
        return inventory;
    }

    public List<Player> getPlayers_start() {
        List<Player> players = new ArrayList<Player>();
        Bukkit.getOnlinePlayers().forEach(player -> {
            if (!isSpectator(player)) players.add(player);
        });
        return players;
    }

    public void playSound(Sound sound, float soundStrength) {
        playSound(sound, soundStrength, soundStrength);
    }

    public void playSound(Sound sound, float soundStrength, float pitch) {
        Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player, sound, soundStrength, pitch));
    }

    public void broadcastTitle(String s1, String s2) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle(s1, s2));
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

    public Player getRandomPlayer() {
        int randomIndex = new Random().nextInt(this.plugin.teamManager.getPlayerTeamList().size());
        return this.plugin.teamManager.getPlayerTeamList().get(randomIndex);
    }

    public String getSpectatorCompass() {
        return "§bTeleporter";
    }

    public List<Player> getSpectatorList() {
        return spectatorList;
    }
}