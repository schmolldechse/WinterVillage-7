package de.voldechse.wintervillage.potterwars.commands;

import de.voldechse.wintervillage.potterwars.PotterWars;
import me.joel.wv6.rangapi.RankAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandBuild implements CommandExecutor {

    public CommandBuild() {
        PotterWars.getInstance().getCommand("build").setExecutor(this);
        PotterWars.getInstance().getCommand("build").setPermission("restinpizza.potterwars.command.build");
    }

    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(PotterWars.getInstance().serverPrefix + "§cUm diesen Befehl ausführen zu können, musst du ein Spieler sein");
            return true;
        }
        Player player = (Player) commandSender;

        final boolean hasPermission = commandSender.isOp() || (RankAPI.instance.isRank((player).getUniqueId().toString(), RankAPI.Rank.ADMIN));
        if (!hasPermission) {
            commandSender.sendMessage("§cIm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
            return true;
        }

        if (PotterWars.getInstance().hasMetadata(player, "BUILD_MODE")) {
            PotterWars.getInstance().removeMetadata(player, "BUILD_MODE");

            player.sendMessage(PotterWars.getInstance().serverPrefix + "§cBuildmodus deaktiviert");
            return true;
        }

        PotterWars.getInstance().setMetadata(player, "BUILD_MODE", true);
        player.sendMessage(PotterWars.getInstance().serverPrefix + "§eBuildmodus aktiviert");
        return true;
    }
}