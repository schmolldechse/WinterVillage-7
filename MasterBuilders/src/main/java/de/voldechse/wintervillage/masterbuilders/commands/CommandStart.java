package de.voldechse.wintervillage.masterbuilders.commands;

import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandStart implements CommandExecutor {
    
    private final MasterBuilders plugin;

    public CommandStart() {
        this.plugin = InjectionLayer.ext().instance(MasterBuilders.class);
        
        this.plugin.getInstance().getCommand("start").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase != Types.LOBBY) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cDas Spiel konnte nicht gestartet werden, da es nicht in Startphase ist");
            return true;
        }

        if (Bukkit.getOnlinePlayers().size() < this.plugin.minPlayers) {
            int missingPlayers = this.plugin.minPlayers - this.plugin.gameManager.getPlayers_start().size();
            commandSender.sendMessage(this.plugin.serverPrefix + "§cUm das Spiel vorzeitig starten zu können, werden noch §b" + missingPlayers + " §cSpieler benötigt");
            return true;
        }

        int startCountdown = 10;
        if (this.plugin.gameStateManager.currentGameState().getCountdown().getCountdownTime() < startCountdown || this.plugin.STARTED) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cDas Spiel startet bereits");
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
        this.plugin.STARTED = true;

        this.plugin.gameStateManager.currentGameState().getCountdown().setCountdownTime(startCountdown);
        commandSender.sendMessage(this.plugin.serverPrefix + "§aDas Spiel wird gestartet");
        return true;
    }
}