package de.voldechse.wintervillage.ttt.commands;

import de.voldechse.wintervillage.ttt.TTT;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandBuild implements CommandExecutor {

    private final TTT plugin;

    public CommandBuild() {
        this.plugin = InjectionLayer.ext().instance(TTT.class);

        this.plugin.getInstance().getCommand("build").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cUm diesen Befehl ausführen zu können, musst du ein Spieler sein");
            return true;
        }
        Player player = (Player) commandSender;

        if (this.plugin.hasMetadata(player, "BUILD_MODE")) {
            this.plugin.removeMetadata(player, "BUILD_MODE");
            player.sendMessage(this.plugin.serverPrefix + "§cBuildmodus deaktiviert");
            return true;
        }

        this.plugin.setMetadata(player, "BUILD_MODE", true);
        player.sendMessage(this.plugin.serverPrefix + "§eBuildmodus aktiviert");
        return true;
    }
}