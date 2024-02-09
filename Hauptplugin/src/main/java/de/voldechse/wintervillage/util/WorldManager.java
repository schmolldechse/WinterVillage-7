package de.voldechse.wintervillage.util;

import de.voldechse.wintervillage.WinterVillage;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WorldManager {

    private final WinterVillage plugin;

    public final Map<String, BossBar> bossBarMap;

    public WorldManager(WinterVillage plugin) {
        this.plugin = plugin;
        this.bossBarMap = new HashMap<>();
    }

    public boolean delete(String name) {
        if (Bukkit.getWorld(name) == null) return false;
        World world = Bukkit.getWorld(name);

        if (!world.getPlayers().isEmpty()) {
            world.getPlayers().forEach(player -> {
                player.teleport(Bukkit.getWorld("world").getSpawnLocation());
                player.sendMessage(this.plugin.serverPrefix + "§cDu wurdest aus der Welt raus teleportiert");
            });
        }

        Bukkit.unloadWorld(world, false);
        try {
            FileUtils.deleteDirectory(world.getWorldFolder());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean create(String name, Type type) {
        if (Bukkit.getWorld(name) != null) return false;

        WorldCreator worldCreator = new WorldCreator(name)
                .generateStructures(true)
                .seed(System.currentTimeMillis());

        switch (type) {
            case OVERWORLD -> {
                worldCreator.environment(World.Environment.NORMAL);
            }

            case NETHER -> {
                worldCreator.environment(World.Environment.NETHER);
            }

            case END -> {
                worldCreator.environment(World.Environment.THE_END);
            }
        }

        Bukkit.createWorld(worldCreator);

        Bukkit.getWorld(name).setGameRule(GameRule.KEEP_INVENTORY, true);
        return true;
    }

    public void preparePreGeneration(String world) {
        if (this.bossBarMap.containsKey(world)) return;

        BossBar bossBar = Bukkit.createBossBar("§f§lFarmwelt Reset §8- §a" + world, BarColor.GREEN, BarStyle.SOLID, BarFlag.CREATE_FOG);
        Bukkit.getOnlinePlayers().forEach(player -> {
            PermissionUser permissionUser = this.plugin.permissionManagement.getOrCreateUserAsync(player.getUniqueId(), player.getName()).join();
            if (permissionUser == null) return;
            PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
            if (permissionGroup == null) return;

            if (permissionGroup.potency() > this.plugin.potencySupporter) bossBar.addPlayer(player);
        });

        bossBar.setProgress(0);
        bossBar.setVisible(true);

        this.bossBarMap.put(world, bossBar);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky world " + world);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky pattern spiral");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky shape circle");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky spawn");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky radius 5000");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "chunky start");
    }

    public enum Type {
        OVERWORLD,
        NETHER,
        END;
    }
}
