package de.voldechse.wintervillage.potterwars.commands;

import de.voldechse.wintervillage.potterwars.PotterWars;
import me.joel.wv6.rangapi.RankAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSetTime implements CommandExecutor {

    public CommandSetTime() {
        PotterWars.getInstance().getCommand("setTime").setExecutor(this);
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        final boolean hasPermission = commandSender.isOp() || (commandSender instanceof Player && RankAPI.instance.isRank(((Player) commandSender).getUniqueId().toString(), RankAPI.Rank.ADMIN));
        if (!hasPermission) {
            commandSender.sendMessage("§cIm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
            return true;
        }

        if (args.length != 1) {
            commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
            commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§a/setTime §8<§aZeit in Sekunden§8>");
            return true;
        }

        int time;
        try {
            time = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored) {
            commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cUngültige Anzahl");
            return true;
        }
        if (time < 1) {
            commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cZahl muss größer 1 sein");
            return true;
        }

        PotterWars.getInstance().gameStateManager.currentGameState().getCountdown().setInitializedTime(time);
        PotterWars.getInstance().gameStateManager.currentGameState().getCountdown().setCountdownTime(time);

        commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§aDie Zeit wurde aktualisiert");
        return true;
    }
}