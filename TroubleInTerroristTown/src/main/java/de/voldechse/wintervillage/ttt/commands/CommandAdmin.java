package de.voldechse.wintervillage.ttt.commands;

import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandAdmin implements CommandExecutor, TabExecutor {

    private final TTT plugin;

    public CommandAdmin() {
        this.plugin = InjectionLayer.ext().instance(TTT.class);

        this.plugin.getInstance().getCommand("admin").setExecutor(this);
        this.plugin.getInstance().getCommand("admin").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cUm diesen Befehl ausführen zu können, musst du ein Spieler sein");
            return true;
        }
        Player player = (Player) commandSender;

        switch (args.length) {
            case 0 -> {
                error(player);
                return true;
            }
            case 1 -> {
                switch (args[0].toUpperCase()) {
                    case "POINTS" -> {
                        this.plugin.roleManager.changeShopPoints(player, 100);

                        if (this.plugin.gameStateManager.currentGameState().getGameStatePhase() == Types.INGAME
                                && this.plugin.roleManager.isPlayerAssigned(player))
                            this.plugin.scoreboardManager.updateScoreboard(player, "currentShopPoints", " " + this.plugin.roleManager.getRole(player).getRolePrefix() + this.plugin.roleManager.getShopPoints(player), "");

                        player.sendMessage(this.plugin.serverPrefix + "§aAktualisiert!");
                        return true;
                    }
                    case "RELOAD_CONFIG" -> {
                        if (this.plugin.gameStateManager.currentGameState().getGameStatePhase() != Types.LOBBY) {
                            player.sendMessage(this.plugin.serverPrefix + "§cDas kannst du nur in der Lobby");
                            return true;
                        }

                        this.plugin.reloadConfigs();

                        player.sendMessage(this.plugin.serverPrefix + "§aNeugeladen!");
                        return true;
                    }
                    default -> {
                        error(player);
                        return true;
                    }
                }
            }
            case 2 -> {
                switch (args[0].toUpperCase()) {
                    case "SETUP" -> {
                        if (this.plugin.gameStateManager.currentGameState().getGameStatePhase() != Types.LOBBY) {
                            player.sendMessage(this.plugin.serverPrefix + "§cDas kannst du nur in der Lobby");
                            return true;
                        }

                        switch (args[1].toUpperCase()) {
                            case "TESTER" -> {
                                this.plugin.setMetadata(player, "SETUP_TESTER", true);
                                player.getInventory().addItem(new ItemBuilder(Material.STICK, 1, "§4SETUP").build());
                                player.setGameMode(GameMode.CREATIVE);

                                player.sendMessage(this.plugin.serverPrefix + "§aDu editierst nun den Tester");
                                player.sendMessage(this.plugin.serverPrefix + "§cSchreibe §eCORNER_A §8| §eCORNER_B §8| §ePLAYER_SPAWN §8| §eOUTSIDE_TESTER §fin den Chat um die Position, an der du stehst, zu speichern.");
                                player.sendMessage(this.plugin.serverPrefix + "§cZerstöre den Block §fmit dem Item, dessen Position du speichern willst. §cSchreibe §fdann entweder §eBARRIER_1 §8| §eBARRIER_2 §8| §eBARRIER_3 §8| §eLEFT_LAMP §8| §eRIGHT_LAMP §8| §eACTIVATION_BUTTON §c(BUTTON) §foder §eTRAITOR_TRAP §c(BUTTON) §fin den Chat, um die Position zu speichern");
                                player.sendMessage(this.plugin.serverPrefix + "§fMit §aDONE §fkannst du den Bearbeitungsmodus verlassen");
                                return true;
                            }
                            case "EDIT_SPAWNS" -> {
                                this.plugin.setMetadata(player, "EDIT_SPAWNS", true);
                                this.plugin.positionManager.editSpawns();

                                player.sendMessage(this.plugin.serverPrefix + "§aDu kannst nun Spawns hinzufügen, indem du einfach §eADD §ain den Chat schreibst");
                                player.sendMessage(this.plugin.serverPrefix + "§eZerstöre einen ArmorStand, wenn du dessen Position löschen möchtest");
                                player.sendMessage(this.plugin.serverPrefix + "§fMit §aDONE §fkannst du den Bearbeitungsmodus verlassen");
                                return true;
                            }
                            case "EDIT_CHESTS" -> {
                                this.plugin.setMetadata(player, "EDIT_CHESTS", true);

                                player.sendMessage(this.plugin.serverPrefix + "§aDu kannst nun Kisten hinzufügen, indem du diese einfach platzierst");
                                player.sendMessage(this.plugin.serverPrefix + "§eZerstöre eine Kiste, wenn du dessen Position löschen möchtest");
                                player.sendMessage(this.plugin.serverPrefix + "§fMit §aDONE §fkannst du den Bearbeitungsmodus verlassen");
                                return true;
                            }
                            case "LOBBY_SPAWN" -> {
                                updateLobbySpawn(player.getLocation());
                                this.plugin.reloadConfigs();

                                player.sendMessage(this.plugin.serverPrefix + "§aGespeichert!");
                            }
                            default -> {
                                error(player);
                                return true;
                            }
                        }
                    }

                    case "TP" -> {
                        String worldName = args[1];
                        if (Bukkit.getWorld(worldName) == null) {
                            player.sendMessage(this.plugin.serverPrefix + "§cDiese Welt existiert nicht");
                            return true;
                        }

                        player.teleport(Bukkit.getWorld(worldName).getSpawnLocation());
                        player.sendMessage(this.plugin.serverPrefix + "§aTeleportiert");
                    }

                    case "IMPORT" -> {
                        String worldName = args[1];

                        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
                        if (!worldFolder.exists() && !worldFolder.isDirectory()) {
                            player.sendMessage(this.plugin.serverPrefix + "§cDiese Welt existiert nicht");
                            return true;
                        }

                        Bukkit.createWorld(WorldCreator.name(worldName));
                        player.sendMessage(this.plugin.serverPrefix + "§aImportiert");
                    }

                    case "DELETE" -> {
                        String worldName = args[1];

                        if (Bukkit.getWorld(worldName) == null) {
                            player.sendMessage(this.plugin.serverPrefix + "§cDiese Welt existiert nicht");
                            return true;
                        }


                        Bukkit.unloadWorld(worldName, false);
                        try {
                            FileUtils.deleteDirectory(new File(worldName));
                        } catch (IOException exception) {
                            throw new RuntimeException(exception);
                        }

                        player.sendMessage(this.plugin.serverPrefix + "§aGelöscht");
                    }

                    default -> {
                        error(player);
                        return true;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        if (!command.getName().equals("admin")) return null;

        List<String> completions = new ArrayList<String>();
        if (args.length == 1) completions.addAll(Arrays.asList("POINTS", "RELOAD_CONFIG", "SETUP", "TP"));
        if (args.length == 2 && args[0].equalsIgnoreCase("SETUP"))
            completions.addAll(Arrays.asList("TESTER", "EDIT_SPAWNS", "EDIT_CHESTS", "LOBBY_SPAWN"));
        if (args.length == 2 && (args[0].equalsIgnoreCase("TP") || args[0].equalsIgnoreCase("DELETE")))
            Bukkit.getWorlds().forEach(world -> completions.add(world.getName()));
        return completions;
    }

    private void updateLobbySpawn(Location location) {
        Document document = this.plugin.configDocument;

        File file = new File(this.plugin.getInstance().getDataFolder().getAbsolutePath(), "config.json");
        file.delete();

        Document newLobbySpawnLocation = new Document("x", location.getX())
                .append("y", location.getY())
                .append("z", location.getZ())
                .append("yaw", location.getYaw())
                .append("pitch", location.getPitch())
                .append("world", location.getWorld().getName());

        document.remove("lobbySpawn");
        document.append("lobbySpawn", newLobbySpawnLocation).saveAsConfig(this.plugin.getInstance().getDataFolder() + "\\config.json");
    }

    private void error(Player player) {
        player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin POINTS");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin RELOAD_CONFIG");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin TP <WORLD>");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin IMPORT <WORLD>");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin DELETE <WORLD>");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin SETUP TESTER");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin SETUP EDIT_SPAWNS");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin SETUP EDIT_CHESTS");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin SETUP LOBBY_SPAWN");
    }
}