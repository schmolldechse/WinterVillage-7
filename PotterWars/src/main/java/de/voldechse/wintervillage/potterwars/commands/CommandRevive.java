package de.voldechse.wintervillage.potterwars.commands;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import de.voldechse.wintervillage.potterwars.team.Team;
import me.joel.wv6.rangapi.RankAPI;
import org.bukkit.Bukkit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandRevive implements CommandExecutor, TabExecutor {

    public CommandRevive() {
        PotterWars.getInstance().getCommand("revive").setExecutor(this);
        PotterWars.getInstance().getCommand("revive").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        final boolean hasPermission = commandSender.isOp() || (commandSender instanceof Player && RankAPI.instance.isRank(((Player) commandSender).getUniqueId().toString(), RankAPI.Rank.ADMIN));
        if (!hasPermission) {
            commandSender.sendMessage("§cIm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
            return true;
        }

        Types gamePhase = PotterWars.getInstance().gameStateManager.currentGameState().getGameStatePhase();

        switch (args.length) {
            case 2 -> {
                if (!args[0].equalsIgnoreCase("team")) {
                    commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
                    commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§a/revive TEAM <TEAM-ID>");
                    commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§a/revive PLAYER <NAME> <TEAM-ID>");
                    return true;
                }

                if (gamePhase != Types.INGAME) {
                    commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cDas geht nur in Ingame-Phase");
                    return true;
                }

                int teamId;
                try {
                    teamId = Integer.parseInt(args[1]);
                } catch (NumberFormatException exception) {
                    commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cUngültige Anzahl");
                    return true;
                }

                if (!PotterWars.getInstance().teamManager.teamInConfig(teamId)) {
                    commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cDas Team §f#" + teamId + " §cist nicht in der Config gespeichert");
                    return true;
                }

                if (PotterWars.getInstance().teamManager.getTeam(teamId) != null) {
                    commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cDas Team §r" + PotterWars.getInstance().teamManager.getTeam(teamId).teamPrefix + PotterWars.getInstance().teamManager.getTeam(teamId).teamPrefix + " §cexistiert bereits");
                    return true;
                }

                Team team = PotterWars.getInstance().teamManager.getTeamFromConfig(teamId);
                PotterWars.getInstance().teamManager.teamList.add(team);

                commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§eDas Team wurde hinzugefügt! Teamdetails:");
                commandSender.sendMessage("§f" + team.toString());
                return true;
            }

            case 3 -> {
                if (!args[0].equalsIgnoreCase("player")) {
                    commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
                    commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§a/revive TEAM <TEAM-ID>");
                    commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§a/revive PLAYER <NAME> <TEAM-ID>");
                    return true;
                }

                if (gamePhase != Types.INGAME) {
                    commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cDas geht nur in Ingame-Phase");
                    return true;
                }

                Player targetPlayer = Bukkit.getPlayer(args[1]);
                if (targetPlayer == null) {
                    commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cDer Spieler §e" + args[0] + " §cist nicht online");
                    return true;
                }

                int teamId;
                try {
                    teamId = Integer.parseInt(args[2]);
                } catch (NumberFormatException exception) {
                    commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cUngültige Anzahl");
                    return true;
                }

                if (!PotterWars.getInstance().teamManager.teamInConfig(teamId)) {
                    commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cDas Team §f#" + teamId + " §cist nicht in der Config gespeichert");
                    return true;
                }

                if (PotterWars.getInstance().teamManager.getTeam(teamId) == null) {
                    commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cFüge das Team erst mit §f/revive TEAM §f" + teamId + " §chinzu");
                    return true;
                }

                Team team = PotterWars.getInstance().teamManager.getTeam(teamId);
                if (team.players.size() >= PotterWars.getInstance().maxPlayersInTeam) {
                    commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cDas Team ist voll");
                    return true;
                }

                if (PotterWars.getInstance().gameManager.isSpectator(targetPlayer))
                    PotterWars.getInstance().gameManager.removeSpectator(targetPlayer);
                PotterWars.getInstance().gameManager.clearPlayer(targetPlayer, true);

                PotterWars.getInstance().teamManager.setCurrentTeam(targetPlayer, teamId);

                Location teamSpawnLocation = new Location(
                        Bukkit.getWorld(team.teamPosition.getWorld()),
                        team.teamPosition.getX(),
                        team.teamPosition.getY(),
                        team.teamPosition.getZ(),
                        team.teamPosition.getYaw(),
                        team.teamPosition.getPitch()
                );
                targetPlayer.teleport(teamSpawnLocation);
                targetPlayer.setHealthScale(40.0);
                targetPlayer.setMaxHealth(40.0);
                targetPlayer.setHealth(40.0);
                targetPlayer.getInventory().setItem(0, new ItemBuilder(Material.STICK, 1, "§cZauberstab").build());
                targetPlayer.sendMessage(PotterWars.getInstance().serverPrefix + "§aDu wurdest wiederbelebt!");
                targetPlayer.playEffect(EntityEffect.TOTEM_RESURRECT);

                commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§7Der Spieler §r" + targetPlayer.getName() + " §7wurde wiederbelebt und in das Team §r" + team.teamPrefix + team.teamName + " §7gesetzt");

                PotterWars.getInstance().scoreboardManager.generateScoreboard();
                return true;
            }

            default -> {
                commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
                commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§a/revive TEAM <TEAM-ID>");
                commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§a/revive PLAYER <NAME> <TEAM-ID>");
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("revive")) {
            switch (strings.length) {
                case 1 -> {
                    completions.addAll(Arrays.asList("TEAM", "PLAYER"));
                }

                case 2 -> {
                    switch (strings[0].toUpperCase()) {
                        case "TEAM" -> {
                            PotterWars.getInstance().teamManager.getTeamList().forEach(team -> completions.add("" + team.teamId));
                        }

                        case "PLAYER" -> {
                            Bukkit.getOnlinePlayers().forEach(player -> completions.add(player.getName()));
                        }

                        default -> {
                        }
                    }
                }

                case 3 -> {
                    if (!strings[0].equalsIgnoreCase("PLAYER")) return null;
                    PotterWars.getInstance().teamManager.getTeamList().forEach(team -> completions.add("" + team.teamId));
                }

                default -> {
                }
            }
        }

        return completions;
    }
}