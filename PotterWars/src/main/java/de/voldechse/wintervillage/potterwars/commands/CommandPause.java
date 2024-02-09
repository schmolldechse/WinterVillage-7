package de.voldechse.wintervillage.potterwars.commands;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.potterwars.PotterWars;
import de.voldechse.wintervillage.potterwars.gamestate.Types;
import me.joel.wv6.rangapi.RankAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandPause implements CommandExecutor {

    public CommandPause() {
        PotterWars.getInstance().getCommand("pause").setExecutor(this);
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        final boolean hasPermission = commandSender.isOp() || (commandSender instanceof Player && RankAPI.instance.isRank(((Player) commandSender).getUniqueId().toString(), RankAPI.Rank.ADMIN));
        if (!hasPermission) {
            commandSender.sendMessage("§cIm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
            return true;
        }

        Types gamePhase = PotterWars.getInstance().gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase != Types.LOBBY) {
            commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cDas konnte nicht pausiert werden, da es nicht in Lobby- und Startphase ist");
            return true;
        }

        Countdown countdown = PotterWars.getInstance().gameStateManager.currentGameState().getCountdown();

        if (PotterWars.getInstance().PAUSED) {
            if (countdown.isSleeping() && PotterWars.getInstance().gameManager.getLivingPlayers().size() >= PotterWars.getInstance().minPlayers) {
                countdown.stopCountdown(false);
                countdown.setInitializedTime(60);
                countdown.startCountdown(60, false);
            }

            commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§aDie Startphase beginnt bald");
            PotterWars.getInstance().STARTED = false;
            PotterWars.getInstance().PAUSED = false;
        } else {
            countdown.stopCountdown(false);
            countdown.sleepCountdown(15);

            PotterWars.getInstance().PAUSED = true;
            commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§aDer Spielstart wird pausiert");
        }
        return true;
    }
}