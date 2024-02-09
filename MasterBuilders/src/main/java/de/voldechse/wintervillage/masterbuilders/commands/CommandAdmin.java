package de.voldechse.wintervillage.masterbuilders.commands;

import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.gamestate.list.GameStateVoteThemes;
import de.voldechse.wintervillage.masterbuilders.teams.Team;
import de.voldechse.wintervillage.masterbuilders.teams.position.Team_Corner;
import de.voldechse.wintervillage.masterbuilders.teams.position.Team_Entity;
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

    private final MasterBuilders plugin;

    public CommandAdmin() {
        this.plugin = InjectionLayer.ext().instance(MasterBuilders.class);

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
            case 0 -> {
                error(player);
                return true;
            }
            
            case 1 -> {
                switch (args[0].toUpperCase()) {
                    case "RELOAD_CONFIG" -> {
                        this.plugin.reloadConfigs();

                        player.sendMessage(this.plugin.serverPrefix + "§aNeugeladen!");
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
            case 2 -> {
                switch (args[0].toUpperCase()) {
                    case "THEME" -> {
                        GameStateVoteThemes.winningTheme = args[1];

                        this.plugin.THEME_ALREADY_SET = true;
                        player.sendMessage(this.plugin.serverPrefix + "Das Thema ist nun §e" + args[1]);
                    }

                    case "ADD_TEAMS" -> {
                        int teams = 0;
                        try {
                            teams = Integer.parseInt(args[1]);
                            if (teams < 1) {
                                commandSender.sendMessage(this.plugin.serverPrefix + "§cDie Anzahl der hinzuzufügenden Teams muss größer als 0 sein");
                                return true;
                            }
                        } catch (NumberFormatException exception) {
                            player.sendMessage(this.plugin.serverPrefix + "§cUngültige Anzahl");
                            return true;
                        }

                        List<Team> teamsBeingAdded = new ArrayList<Team>();
                        int highestTeamId = getHighestTeamId();

                        for (int i = 0; i < teams; i++) {
                            int newTeamId = highestTeamId + 1;

                            Team_Corner testCorner = new Team_Corner(0.0, 0.0, 0.0, "world");
                            Team_Entity entitySpawn = new Team_Entity(0.0, 0.0, 0.0, 0.0F, 0.0F, "world");
                            Team team = new Team(newTeamId, testCorner, testCorner, entitySpawn, entitySpawn, new ArrayList<Player>(), 0);

                            teamsBeingAdded.add(team);
                            highestTeamId = newTeamId;
                        }

                        this.plugin.teamManager.saveTeamsToConfig(teamsBeingAdded);
                        commandSender.sendMessage(this.plugin.serverPrefix + "§e" + teams + " §fneue(s) Team(s) wurde(n) hinzugefügt");
                        return true;
                    }

                    case "EDIT_TEAM" -> {
                        int teamId = 0;
                        try {
                            teamId = Integer.parseInt(args[1]);
                        } catch (NumberFormatException exception) {
                            player.sendMessage(this.plugin.serverPrefix + "§cUngültige Anzahl");
                            return true;
                        }

                        if (!this.plugin.teamManager.teamInConfig(teamId)) {
                            player.sendMessage(this.plugin.serverPrefix + "§d" + teamId + " §cist nicht in der Config gespeichert");
                            return true;
                        }

                        this.plugin.setMetadata(player, "EDIT_TEAM", teamId);

                        player.sendMessage(this.plugin.serverPrefix + "Du editierst nun das Team §e#" + teamId);
                        player.sendMessage(this.plugin.serverPrefix + "§fSchreibe §eCORNER_A §8| §eCORNER_B §8| §ePLAYER_SPAWN §8| §eVILLAGER_SPAWN §fin den Chat, um die Position zu speichern oder §aDONE §fum den Bearbeitungsmodus zu verlassen");
                        return true;
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
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equals("admin")) return null;
        List<String> completions = new ArrayList<String>();
        if (args.length == 1)
            completions.addAll(Arrays.asList("RELOAD_CONFIG", "LOBBY_SPAWN", "THEME", "EDIT_TEAM", "TP", "ADD_TEAMS"));
        if (args.length == 2 && (args[0].equalsIgnoreCase("TP")))
            Bukkit.getWorlds().forEach(world -> completions.add(world.getName()));
        if (args.length == 2 && args[0].equalsIgnoreCase("EDIT_TEAM"))
            this.plugin.teamManager.getTeamIds().forEach(teamId -> completions.add(" " + teamId));
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
        document.append("lobbySpawn", newLobbySpawnLocation).saveAsConfig(Paths.get(this.plugin.getInstance().getDataFolder().getAbsolutePath(), "config.json"));
    }

    private int getHighestTeamId() {
        int highestTeamId = -1;
        for (Team team : this.plugin.teamManager.getTeamList())
            if (team.teamId > highestTeamId) highestTeamId = team.teamId;
        return highestTeamId;
    }

    private void error(Player player) {
        player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin RELOAD_CONFIG");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin LOBBY_SPAWN");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin THEME <THEME>");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin EDIT_TEAM <TEAM-ID>");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin ADD_TEAMS <INTEGER>");
        player.sendMessage(this.plugin.serverPrefix + "§a/admin TP <WORLD>");
    }
}