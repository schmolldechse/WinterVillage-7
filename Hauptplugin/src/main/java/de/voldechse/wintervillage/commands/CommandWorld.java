package de.voldechse.wintervillage.commands;

import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.util.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class CommandWorld implements CommandExecutor {

    private final WinterVillage plugin;

    public CommandWorld(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("world").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cPlease execute this command as a player");
            return true;
        }

        switch (args.length) {
            case 2 -> {
                String worldName = "";
                if (args[1].equalsIgnoreCase("farmwelt"))
                    worldName = "world_farmwelt";
                else worldName = args[1];

                if (Bukkit.getWorld(worldName) == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cDiese Welt existiert nicht");
                    return true;
                }

                WorldManager.Type worldType = WorldManager.Type.OVERWORLD;

                String chat = "";
                switch (worldName) {
                    case "world_farmwelt" -> {
                        chat = "§eDie Farmwelt";
                        worldType = WorldManager.Type.OVERWORLD;
                    }

                    default -> {
                        switch (Bukkit.getWorld(worldName).getEnvironment()) {
                            case NORMAL, CUSTOM -> {
                                chat = "§eDie Welt §c" + worldName;
                                worldType = WorldManager.Type.OVERWORLD;
                            }

                            case NETHER -> {
                                chat = "§eDer Nether";
                                worldType = WorldManager.Type.NETHER;
                            }

                            case THE_END -> {
                                chat = "§eDas Ende";
                                worldType = WorldManager.Type.END;
                            }
                        }
                    }
                }

                if (args[0].equalsIgnoreCase("reset")) {
                    if (worldName.equalsIgnoreCase("world")) {
                        player.sendMessage(this.plugin.serverPrefix + "§cDie Bauwelt kann nicht zurückgesetzt werden");
                        return true;
                    }

                    if (!Bukkit.getWorld(worldName).getPlayers().isEmpty()) {
                        Bukkit.broadcastMessage(this.plugin.serverPrefix + chat + " §ewird in 30 Sekunden zurückgesetzt");
                        Bukkit.broadcastMessage(this.plugin.serverPrefix + "§cAlle sich noch darin befindenden Spieler werden kurz bevor raus teleportiert");

                        WorldManager.Type finalWorldType = worldType;

                        String finalWorldName = worldName;
                        String finalChat = chat;
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                plugin.worldManager.delete(finalWorldName);
                                plugin.worldManager.create(finalWorldName, finalWorldType);

                                Bukkit.broadcastMessage(plugin.serverPrefix + finalChat + " §ewurde zurückgesetzt");
                            }
                        }.runTaskLater(this.plugin.getInstance(), 30 * 20L);
                    } else {
                        Bukkit.broadcastMessage(this.plugin.serverPrefix + chat + " §ewird zurückgesetzt");

                        this.plugin.worldManager.delete(worldName);
                        this.plugin.worldManager.create(worldName, worldType);

                        Bukkit.broadcastMessage(this.plugin.serverPrefix + chat + " §ewurde zurückgesetzt");
                    }
                    return true;
                }

                if (args[0].equalsIgnoreCase("delete")) {
                    if (worldName.equalsIgnoreCase("world")) {
                        player.sendMessage(this.plugin.serverPrefix + "§cDie Bauwelt kann nicht gelöscht werden");
                        return true;
                    }

                    if (!Bukkit.getWorld(worldName).getPlayers().isEmpty()) {
                        Bukkit.broadcastMessage(this.plugin.serverPrefix + chat + " §ewird in 30 Sekunden zurückgesetzt");

                        String finalWorldName = worldName;
                        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin.getInstance(), () -> {
                            this.plugin.worldManager.delete(finalWorldName);
                        }, 30 * 20L);
                    } else {
                        this.plugin.worldManager.delete(worldName);
                        this.plugin.worldManager.create(worldName, worldType);
                    }
                    return true;
                }

                if (args[0].equalsIgnoreCase("teleport")
                        || args[0].equalsIgnoreCase("tp")) {
                    player.teleport(Bukkit.getWorld(worldName).getSpawnLocation());
                    player.sendMessage(this.plugin.serverPrefix + "§aTeleportiert!");
                    return true;
                }

                error(player);
            }

            default -> error(player);
        }
        return false;
    }

    private void error(Player player) {
        player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
        player.sendMessage(this.plugin.serverPrefix + "§a/world reset <Weltname>");
        player.sendMessage(this.plugin.serverPrefix + "§a/world delete <Weltname>");
        player.sendMessage(this.plugin.serverPrefix + "§a/world tp <Weltname>");
    }
}