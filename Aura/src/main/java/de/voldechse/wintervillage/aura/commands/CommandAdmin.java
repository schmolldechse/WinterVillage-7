package de.voldechse.wintervillage.aura.commands;

import de.voldechse.wintervillage.aura.Aura;
import de.voldechse.wintervillage.library.document.Document;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandAdmin implements CommandExecutor, TabExecutor {

    private final Aura plugin;

    public CommandAdmin() {
        this.plugin = InjectionLayer.ext().instance(Aura.class);

        this.plugin.getInstance().getCommand("admin").setExecutor(this);
        this.plugin.getInstance().getCommand("admin").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cUm diesen Befehl ausführen zu können, musst du ein Spieler sein");
            return true;
        }

        switch (args.length) {
            case 1 -> {
                switch (args[0].toUpperCase()) {
                    case "RELOAD_CONFIG" -> {
                        this.plugin.getInstance().reloadConfig();

                        player.sendMessage(this.plugin.serverPrefix + "§aNeugeladen!");
                        return true;
                    }

                    case "LOBBY_SPAWN" -> {
                        update("lobby", player.getLocation());
                        this.plugin.getInstance().reloadConfig();

                        player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                    }

                    case "PLAYER_SPAWN" -> {
                        update("player", player.getLocation());
                        this.plugin.getInstance().reloadConfig();

                        player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                    }

                    default -> {
                        this.error(player);
                        return true;
                    }
                }
            }
            case 2 -> {
                if (args[0].toUpperCase().equals("TP")) {
                    String worldName = args[1];
                    if (Bukkit.getWorld(worldName) == null) {
                        player.sendMessage(this.plugin.serverPrefix + "§cDiese Welt existiert nicht");
                        return true;
                    }

                    player.teleport(Bukkit.getWorld(worldName).getSpawnLocation());
                    player.sendMessage(this.plugin.serverPrefix + "§aTeleportiert");
                } else this.error(player);
            }

            default -> {
                this.error(player);
                return true;
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("admin")) {
            switch (args.length) {
                case 1 -> {
                    completions.addAll(Arrays.asList("RELOAD_CONFIG", "LOBBY_SPAWN", "PLAYER_SPAWN", "TP"));
                }

                case 2 -> {
                    if (args[0].toUpperCase().equals("TP")) {
                        Bukkit.getWorlds().forEach(world -> completions.add(world.getName()));
                    }
                }

                default -> {}
            }
        }

        return completions;
    }

    private void update(String option, Location location) {
        Document document = this.plugin.configDocument;

        Document update = new Document("x", location.getX())
                .append("y", location.getY())
                .append("z", location.getZ())
                .append("yaw", location.getYaw())
                .append("pitch", location.getPitch())
                .append("world", location.getWorld().getName());

        switch (option.toUpperCase()) {
            case "LOBBY" -> {
                document.remove("lobbySpawn");
                document.append("lobbySpawn", update);
            }

            case "PLAYER" -> {
                document.remove("playerSpawn");
                document.append("playerSpawn", update);
            }

            default -> {
                this.plugin.getInstance().getLogger().severe("Could not find '" + option + "' as option");
                return;
            }
        }

        File file = new File(this.plugin.getInstance().getDataFolder().getAbsolutePath(), "config.json");
        file.delete();

        document.saveAsConfig(Paths.get(this.plugin.getInstance().getDataFolder().getAbsolutePath(), "config.json"));
    }

    private void error(Player player) {
        player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin RELOAD_CONFIG");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin LOBBY_SPAWN");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin PLAYER_SPAWN");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin TP <WORLD>");
    }
}