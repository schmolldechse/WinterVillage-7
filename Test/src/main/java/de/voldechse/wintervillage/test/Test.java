package de.voldechse.wintervillage.test;

import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.event.player.ChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.popcraft.chunky.Chunky;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Test extends JavaPlugin implements CommandExecutor {

    private static Test instance;

    private MojangAPI mojangAPI;

    public WorldManager worldManager;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        instance = this;

        this.mojangAPI = new MojangAPI();

        this.worldManager = new WorldManager(this);

        getCommand("world").setExecutor(this);

        getServer().getServicesManager().getRegistration(Chunky.class).getProvider().getApi().onGenerationProgress(generationProgressEvent -> {
            if (this.worldManager.bossBarMap.containsKey(generationProgressEvent.world())) {
                BossBar bossBar = this.worldManager.bossBarMap.get(generationProgressEvent.world());
                bossBar.setProgress((float) generationProgressEvent.progress() / 100);

                float value = BigDecimal.valueOf(generationProgressEvent.progress())
                        .setScale(2, RoundingMode.HALF_UP)
                        .floatValue();

                String finished = String.format("%02d:%02d:%02d", generationProgressEvent.hours(), generationProgressEvent.minutes(), generationProgressEvent.seconds());
                bossBar.setTitle("§a" + generationProgressEvent.world() + " §fvorladen §8- §b" + value + " % §8| §ffertig in §c" + finished);

                if (generationProgressEvent.complete()) {
                    bossBar.setProgress(1);
                    bossBar.setVisible(false);
                    this.worldManager.bossBarMap.remove(generationProgressEvent.world());
                }
            }
        });
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cPlease execute this command as a player");
            return true;
        }

        switch (args.length) {
            case 2 -> {
                String worldName = "";
                if (args[1].equalsIgnoreCase("farmwelt"))
                    worldName = "world_farmwelt";
                else worldName = args[1];

                if (Bukkit.getWorld(worldName) == null) {
                    player.sendMessage("§cDiese Welt existiert nicht");
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
                        player.sendMessage("§cDie Bauwelt kann nicht zurückgesetzt werden");
                        return true;
                    }

                    if (!Bukkit.getWorld(worldName).getPlayers().isEmpty()) {
                        Bukkit.broadcastMessage(chat + " §ewird in 30 Sekunden zurückgesetzt");
                        Bukkit.broadcastMessage("§cAlle sich noch darin befindenden Spieler werden kurz bevor raus teleportiert");

                        WorldManager.Type finalWorldType = worldType;

                        String finalWorldName = worldName;
                        String finalChat = chat;
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                worldManager.delete(finalWorldName);
                                worldManager.create(finalWorldName, finalWorldType);

                                Bukkit.broadcastMessage(finalChat + " §ewurde zurückgesetzt");
                            }
                        }.runTaskLater(instance, 30 * 20L);
                    } else {
                        Bukkit.broadcastMessage(chat + " §ewird zurückgesetzt");

                        worldManager.delete(worldName);
                        worldManager.create(worldName, worldType);

                        Bukkit.broadcastMessage(chat + " §ewurde zurückgesetzt");
                    }
                    return true;
                }

                if (args[0].equalsIgnoreCase("teleport")
                        || args[0].equalsIgnoreCase("tp")) {
                    player.teleport(Bukkit.getWorld(worldName).getSpawnLocation());
                    player.sendMessage("§aTeleportiert!");
                    return true;
                }

                error(player);
            }

            default -> error(player);
        }
        return false;
    }

    private void error(Player player) {
        player.sendMessage("§cFalsche Syntax! Verwende folgendermaßen:");
        player.sendMessage("§a/lol reset <Weltname>");
        player.sendMessage("§a/lol tp <Weltname>");
    }
}