package de.voldechse.wintervillage.masterbuilders.commands;

import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandPause implements CommandExecutor {
    
    private final MasterBuilders plugin;

    public CommandPause() {
        this.plugin = InjectionLayer.ext().instance(MasterBuilders.class);
        
        this.plugin.getInstance().getCommand("pause").setExecutor(this);
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase != Types.LOBBY) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cDas Spiel konnte nicht pausiert werden, da es nicht in Lobbyphase ist");
            return true;
        }

        Countdown countdown = this.plugin.gameStateManager.currentGameState().getCountdown();

        if (this.plugin.PAUSED) {
            if (countdown.isSleeping() && this.plugin.gameManager.getPlayers_start().size() >= this.plugin.minPlayers) {
                countdown.stopCountdown(false);
                countdown.setInitializedTime(this.plugin.lobbyCountdown);
                countdown.startCountdown(this.plugin.lobbyCountdown, false);
            }

            commandSender.sendMessage(this.plugin.serverPrefix + "§aDie Startphase beginnt bald");
            this.plugin.STARTED = false;
            this.plugin.PAUSED = false;
        } else {
            countdown.stopCountdown(false);
            countdown.sleepCountdown(15);

            this.plugin.PAUSED = true;
            commandSender.sendMessage(this.plugin.serverPrefix + "§aDer Spielstart wird pausiert");
        }
        return true;
    }
}