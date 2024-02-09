package de.voldechse.wintervillage.potterwars.commands;

import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import me.joel.wv6.rangapi.RankAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandStart implements CommandExecutor {

    public CommandStart() {
        PotterWars.getInstance().getCommand("start").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        final boolean hasPermission = commandSender.isOp() || (commandSender instanceof Player && RankAPI.instance.isRank(((Player) commandSender).getUniqueId().toString(), RankAPI.Rank.ADMIN));
        if (!hasPermission) {
            commandSender.sendMessage("§cIm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
            return true;
        }

        Types gamePhase = PotterWars.getInstance().gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase != Types.LOBBY) {
            commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cDas Spiel konnte nicht gestartet werden, da es nicht in Startphase ist");
            return true;
        }
        if (Bukkit.getOnlinePlayers().size() < PotterWars.getInstance().minPlayers) {
            int missingPlayers = PotterWars.getInstance().minPlayers - PotterWars.getInstance().gameManager.getLivingPlayers().size();
            commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cUm das Spiel vorzeitig starten zu können, werden noch §b" + missingPlayers + " §cSpieler benötigt");
            return true;
        }
        int startCountdown = 10;
        if (PotterWars.getInstance().gameStateManager.currentGameState().getCountdown().getCountdownTime() < startCountdown || PotterWars.getInstance().STARTED) {
            commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cDas Spiel startet bereits");
            return true;
        }
        if (args.length == 1) {
            try {
                startCountdown = Integer.parseInt(args[0]);
            } catch (NumberFormatException exception) {
                commandSender.sendMessage("§cUngültige Anzahl");
                return true;
            }
        }

        PotterWars.getInstance().STARTED = true;
        PotterWars.getInstance().gameStateManager.currentGameState().getCountdown().setCountdownTime(startCountdown);
        commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§aDas Spiel wird gestartet");
        return true;
    }
}