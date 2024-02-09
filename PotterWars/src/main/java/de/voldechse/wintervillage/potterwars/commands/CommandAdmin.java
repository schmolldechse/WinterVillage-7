package de.voldechse.wintervillage.potterwars.commands;

import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import de.voldechse.wintervillage.potterwars.team.Team;
import de.voldechse.wintervillage.potterwars.team.position.TeamPosition;
import me.joel.wv6.rangapi.RankAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WorldCreator;
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

    public CommandAdmin() {
        PotterWars.getInstance().getCommand("admin").setExecutor(this);
        PotterWars.getInstance().getCommand("admin").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cUm diesen Befehl ausführen zu können, musst du ein Spieler sein");
            return true;
        }
        Player player = (Player) commandSender;

        final boolean hasPermission = commandSender.isOp() || (RankAPI.instance.isRank(((Player) commandSender).getUniqueId().toString(), RankAPI.Rank.ADMIN));
        if (!hasPermission) {
            commandSender.sendMessage("§cIm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
            return true;
        }

        switch (args.length) {
            case 0 -> {
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin RELOAD_CONFIG");
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin LOBBY_SPAWN");
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin TEAMS");
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin EDIT_TEAM <TEAM-ID>");
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin ADD_TEAMS <INTEGER>");
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin JOIN_TEAM <TEAM-ID>");
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin TP <WORLD>");
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin IMPORT <WORLD>");
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin LEVEL <SPIELER> <ANZAHL>");
                return true;
            }
            case 1 -> {
                switch (args[0].toUpperCase()) {
                    case "RELOAD_CONFIG" -> {
                        PotterWars.getInstance().reloadConfigs();

                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§aNeugeladen!");
                        return true;
                    }

                    case "LOBBY_SPAWN" -> {
                        updateLobbySpawn(player.getLocation());
                        PotterWars.getInstance().reloadConfigs();

                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§aGespeichert!");
                    }

                    case "TEAMS" -> {
                        commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§7Folgende Teams sind in der Config gespeichert§8:");

                        PotterWars.getInstance().teamManager.getTeamsFromConfig().forEach(team -> {
                            commandSender.sendMessage("§8- §f" + team.toString().replace("§", "&") + (PotterWars.getInstance().teamManager.teamAlive(team) ? " §8(§aAM LEBEN§8)" : " §8(§cELIMINIERT§8)"));
                        });
                        return true;
                    }

                    default -> {
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin RELOAD_CONFIG");
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin LOBBY_SPAWN");
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin TEAMS");
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin EDIT_TEAM <TEAM-ID>");
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin ADD_TEAMS <INTEGER>");
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin JOIN_TEAM <TEAM-ID>");
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin TP <WORLD>");
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin IMPORT <WORLD>");
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin LEVEL <SPIELER> <ANZAHL>");
                        return true;
                    }
                }
            }
            case 2 -> {
                switch (args[0].toUpperCase()) {
                    case "ADD_TEAMS" -> {
                        int teams = 0;
                        try {
                            teams = Integer.parseInt(args[1]);
                            if (teams < 1) {
                                commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cDie Anzahl der hinzuzufügenden Teams muss größer als 0 sein");
                                return true;
                            }
                        } catch (NumberFormatException exception) {
                            player.sendMessage(PotterWars.getInstance().serverPrefix + "§cUngültige Anzahl");
                            return true;
                        }

                        List<Team> teamsBeingAdded = new ArrayList<Team>();
                        int highestTeamId = getHighestTeamId();

                        for (int i = 0; i < teams; i++) {
                            int newTeamId = highestTeamId + 1;

                            TeamPosition teamPosition = new TeamPosition(0.0, 0.0, 0.0, 0.0f, 0.0f, "world");

                            Team team = new Team("WHITE_WOOL", newTeamId, "TEAM_" + newTeamId, "§f", new ArrayList<>(), teamPosition);

                            teamsBeingAdded.add(team);
                            highestTeamId = newTeamId;
                        }

                        PotterWars.getInstance().teamManager.saveTeamsToConfig(teamsBeingAdded);
                        commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§e" + teams + " §fneue(s) Team(s) wurde(n) hinzugefügt");
                        return true;
                    }

                    case "EDIT_TEAM" -> {
                        int teamId = 0;
                        try {
                            teamId = Integer.parseInt(args[1]);
                        } catch (NumberFormatException exception) {
                            player.sendMessage(PotterWars.getInstance().serverPrefix + "§cUngültige Anzahl");
                            return true;
                        }

                        if (!PotterWars.getInstance().teamManager.teamInConfig(teamId)) {
                            player.sendMessage(PotterWars.getInstance().serverPrefix + "§d" + teamId + " §cist nicht in der Config gespeichert");
                            return true;
                        }

                        PotterWars.getInstance().setMetadata(player, "EDIT_TEAM", teamId);

                        player.sendMessage(PotterWars.getInstance().serverPrefix + "Du editierst nun das Team §e#" + teamId);
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§fSchreibe §eTEAM_BLOCK §fwährend du ein Item in der Hand hältst, §eTEAM_NAME §8| §eTEAM_PREFIX §8<§eARGUMENT§8> §foder §ePLAYERSPAWN §fin den Chat, um die Position zu speichern oder §aDONE §fum den Bearbeitungsmodus zu verlassen");
                        return true;
                    }

                    case "JOIN_TEAM" -> {
                        int teamId = 0;
                        try {
                            teamId = Integer.parseInt(args[1]);
                        } catch (NumberFormatException exception) {
                            player.sendMessage(PotterWars.getInstance().serverPrefix + "§cUngültige Anzahl");
                            return true;
                        }

                        if (!PotterWars.getInstance().teamManager.teamInConfig(teamId)
                                && PotterWars.getInstance().teamManager.getTeam(teamId) == null) {

                            StringBuilder stringBuilder = new StringBuilder();
                            for (Team team : PotterWars.getInstance().teamManager.getTeamList()) {
                                if (stringBuilder.length() > 0) stringBuilder.append("§8, ");
                                stringBuilder.append("§f").append(team.teamId);
                            }

                            commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cDas Team §f#" + teamId + " §cist nicht existent.");
                            commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§7Folgende Teams sind existent§8: §r" + stringBuilder.toString());
                            return true;
                        }

                        if (PotterWars.getInstance().gameManager.isSpectator(player)) {
                            player.sendMessage(PotterWars.getInstance().serverPrefix + "§cNutze dafür §a/revive player §f" + player.getName() + " " + teamId);
                            return true;
                        }

                        Team team = PotterWars.getInstance().teamManager.getTeam(teamId);
                        if (team.players.size() == PotterWars.getInstance().maxPlayersInTeam) {
                            commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cDas Team §r" + team.teamPrefix + team.teamName + " §cist bereits voll");
                            return true;
                        }

                        PotterWars.getInstance().teamManager.setCurrentTeam(player, teamId);

                        commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§7Du bist dem Team Spieler §r" + team.teamPrefix + team.teamName + " §7beigetreten");

                        if (PotterWars.getInstance().gameStateManager.currentGameState().getGameStatePhase() == Types.INGAME) {
                            String worldName = team.teamPosition.getWorld();

                            double positionX = team.teamPosition.getX();
                            double positionY = team.teamPosition.getY();
                            double positionZ = team.teamPosition.getZ();

                            float positionYaw = team.teamPosition.getYaw();
                            float positionPitch = team.teamPosition.getPitch();
                            Location location = new Location(Bukkit.getWorld(worldName), positionX, positionY, positionZ, positionYaw, positionPitch);
                            player.teleport(location);

                            PotterWars.getInstance().scoreboardManager.generateScoreboard(player);
                        }
                        return true;
                    }

                    case "TP" -> {
                        String worldName = args[1];
                        if (Bukkit.getWorld(worldName) == null) {
                            player.sendMessage(PotterWars.getInstance().serverPrefix + "§cDiese Welt existiert nicht");
                            return true;
                        }

                        player.teleport(Bukkit.getWorld(worldName).getSpawnLocation());
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§aTeleportiert");
                    }

                    case "IMPORT" -> {
                        String worldName = args[1];

                        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
                        if (!worldFolder.exists() && !worldFolder.isDirectory()) {
                            player.sendMessage(PotterWars.getInstance().serverPrefix + "§cDiese Welt existiert nicht");
                            return true;
                        }

                        Bukkit.createWorld(WorldCreator.name(worldName));
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§aImportiert");
                    }

                    default -> {
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin RELOAD_CONFIG");
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin LOBBY_SPAWN");
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin TEAMS");
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin EDIT_TEAM <TEAM-ID>");
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin ADD_TEAMS <INTEGER>");
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin JOIN_TEAM <TEAM-ID>");
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin TP <WORLD>");
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin IMPORT <WORLD>");
                        player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin LEVEL <SPIELER> <ANZAHL>");
                    }
                }
            }

            case 3 -> {
                if (!args[0].equalsIgnoreCase("LEVEL")) {
                    player.sendMessage(PotterWars.getInstance().serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
                    player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin RELOAD_CONFIG");
                    player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin LOBBY_SPAWN");
                    player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin TEAMS");
                    player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin EDIT_TEAM <TEAM-ID>");
                    player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin ADD_TEAMS <INTEGER>");
                    player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin JOIN_TEAM <TEAM-ID>");
                    player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin TP <WORLD>");
                    player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin IMPORT <WORLD>");
                    player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin LEVEL <SPIELER> <ANZAHL>");
                    return true;
                }

                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer == null) {
                    commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cDer Spieler §b" + args[0] + " §cist nicht online.");
                    return true;
                }

                int level;
                try {
                    level = Integer.parseInt(args[2]);
                } catch (NumberFormatException exception) {
                    player.sendMessage(PotterWars.getInstance().serverPrefix + "§cUngültige Anzahl");
                    return true;
                }

                if (!PotterWars.getInstance().teamManager.isPlayerInTeam(targetPlayer)) {
                    player.sendMessage(PotterWars.getInstance().serverPrefix + "§f" + targetPlayer.getName() + " §cist in keinem Team");
                    return true;
                }

                Team team = PotterWars.getInstance().teamManager.getTeam(targetPlayer);

                targetPlayer.setLevel(targetPlayer.getLevel() + level);
                commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§7Dem Spieler §f" + team.teamPrefix + targetPlayer.getName() + " §7wurden §c" + level + " §7Level gutgeschrieben");
                return true;
            }

            default -> {
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin RELOAD_CONFIG");
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin LOBBY_SPAWN");
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin TEAMS");
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin EDIT_TEAM <TEAM-ID>");
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin ADD_TEAMS <INTEGER>");
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin JOIN_TEAM <TEAM-ID>");
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin TP <WORLD>");
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin IMPORT <WORLD>");
                player.sendMessage(PotterWars.getInstance().serverPrefix + "§a/admin LEVEL <SPIELER> <ANZAHL>");
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
                    completions.addAll(Arrays.asList("RELOAD_CONFIG", "LOBBY_SPAWN", "TEAMS", "EDIT_TEAM", "ADD_TEAMS", "JOIN_TEAM", "TP", "LEVEL"));
                }

                case 2 -> {
                    switch (args[0].toUpperCase()) {
                        case "EDIT_TEAM", "JOIN_TEAM" -> {
                            PotterWars.getInstance().teamManager.getTeamList().forEach(team -> completions.add("" + team.teamId));
                        }

                        case "TP" -> {
                            Bukkit.getWorlds().forEach(world -> completions.add(world.getName()));
                        }

                        case "LEVEL" -> {
                            Bukkit.getOnlinePlayers().forEach(player -> completions.add(player.getName()));
                        }

                        default -> {
                        }
                    }
                }

                default -> {
                }
            }
        }

        return completions;
    }

    private void updateLobbySpawn(Location location) {
        Document document = PotterWars.getInstance().configDocument;

        File file = new File(PotterWars.getInstance().getDataFolder().getAbsolutePath(), "config.json");
        file.delete();

        Document newLobbySpawnLocation = new Document("x", location.getX())
                .append("y", location.getY())
                .append("z", location.getZ())
                .append("yaw", location.getYaw())
                .append("pitch", location.getPitch())
                .append("world", location.getWorld().getName());

        document.remove("lobbySpawn");
        document.append("lobbySpawn", newLobbySpawnLocation)
                .saveAsConfig(Paths.get(PotterWars.getInstance().getDataFolder().getAbsolutePath(), "config.json"));
    }

    private int getHighestTeamId() {
        int highestTeamId = -1;
        for (Team team : PotterWars.getInstance().teamManager.getTeamList())
            if (team.teamId > highestTeamId) highestTeamId = team.teamId;
        return highestTeamId;
    }
}