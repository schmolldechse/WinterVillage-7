package de.voldechse.wintervillage.ttt.commands;

import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandStart implements CommandExecutor {

    private final TTT plugin;

    public CommandStart() {
        this.plugin = InjectionLayer.ext().instance(TTT.class);

        this.plugin.getInstance().getCommand("start").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase != Types.LOBBY) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cDas kannst du nur in der Lobby");
            return true;
        }
        if (Bukkit.getOnlinePlayers().size() < this.plugin.minPlayers) {
            int missingPlayers = this.plugin.minPlayers - this.plugin.roleManager.getPlayerList().size();
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
                commandSender.sendMessage(this.plugin.serverPrefix + "§cUngültige Anzahl");
                return true;
            }
        }
        this.plugin.STARTED = true;

        this.plugin.gameStateManager.currentGameState().getCountdown().setCountdownTime(startCountdown);
        commandSender.sendMessage(this.plugin.serverPrefix + "§aDas Spiel wird gestartet");
        return true;
    }
}