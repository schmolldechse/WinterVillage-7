package de.voldechse.wintervillage.commands;

import de.voldechse.wintervillage.WinterVillage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandSetHome implements CommandExecutor {

    private final WinterVillage plugin;

    public CommandSetHome(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("setHome").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cPlease execute this command as a player");
            return true;
        }

        if (this.plugin.homeDatabase.saved(player.getUniqueId()))
            this.plugin.homeDatabase.remove(player.getUniqueId());

        this.plugin.homeDatabase.save(player.getUniqueId(), player.getLocation());
        player.sendMessage(this.plugin.serverPrefix + "§aDu hast dein Zuhause auf deine derzeitige Position gespeichert");
        return true;
    }
}
