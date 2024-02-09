package de.voldechse.wintervillage.masterbuilders.commands;

import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandSetTime implements CommandExecutor {
    
    private final MasterBuilders plugin;

    public CommandSetTime() {
        this.plugin = InjectionLayer.ext().instance(MasterBuilders.class);
        
        this.plugin.getInstance().getCommand("setTime").setExecutor(this);
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (args.length != 1) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
            commandSender.sendMessage(this.plugin.serverPrefix + "§a/setTime §8<§aZeit in Sekunden§8>");
            return true;
        }

        int time;
        try {
            time = Integer.parseInt(args[0]);
        } catch (NumberFormatException ignored) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cUngültige Anzahl");
            return true;
        }
        if (time < 1) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cZahl muss größer 1 sein");
            return true;
        }

        if (this.plugin.gameStateManager.currentGameState().getGameStatePhase() == Types.VOTING_BUILDINGS) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cDas ist während des Bewertens nicht möglich");
            return true;
        }

        this.plugin.gameStateManager.currentGameState().getCountdown().setInitializedTime(time);
        this.plugin.gameStateManager.currentGameState().getCountdown().setCountdownTime(time);

        commandSender.sendMessage(this.plugin.serverPrefix + "§aDie Zeit wurde aktualisiert");
        return true;
    }
}