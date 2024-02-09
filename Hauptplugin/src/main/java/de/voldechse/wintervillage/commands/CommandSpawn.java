package de.voldechse.wintervillage.commands;

import de.voldechse.wintervillage.WinterVillage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandSpawn implements CommandExecutor {

    private final WinterVillage plugin;

    public CommandSpawn(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("spawn").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cPlease execute this command as a player");
            return true;
        }

        if (Bukkit.getWorld("world") == null) {
            player.sendMessage(this.plugin.serverPrefix + "§cDie Bauwelt konnte nicht gefunden werden");
            return true;
        }

        player.teleport(Bukkit.getWorld("world").getSpawnLocation());
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        return false;
    }
}
