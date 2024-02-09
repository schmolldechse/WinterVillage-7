package de.voldechse.wintervillage.aura.commands;

import de.voldechse.wintervillage.aura.Aura;
import de.voldechse.wintervillage.aura.gamestate.Types;
import de.voldechse.wintervillage.library.countdown.Countdown;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandPause implements CommandExecutor {

    private final Aura plugin;
    
    public CommandPause() {
        this.plugin = InjectionLayer.ext().instance(Aura.class);
        
        this.plugin.getInstance().getCommand("pause").setExecutor(this);
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase != Types.LOBBY) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cDas konnte nicht pausiert werden, da es nicht in Lobby- und Startphase ist");
            return true;
        }

        Countdown countdown = this.plugin.gameStateManager.currentGameState().getCountdown();

        if (this.plugin.PAUSED) {
            if (countdown.isSleeping() && this.plugin.gameManager.getLivingPlayers().size() >= this.plugin.minPlayers) {
                countdown.stopCountdown(false);
                countdown.setInitializedTime(60);
                countdown.startCountdown(60, false);
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