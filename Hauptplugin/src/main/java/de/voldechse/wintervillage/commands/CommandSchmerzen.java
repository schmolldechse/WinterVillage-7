package de.voldechse.wintervillage.commands;

import de.voldechse.wintervillage.WinterVillage;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CommandSchmerzen implements CommandExecutor {

    private final WinterVillage plugin;

    public CommandSchmerzen(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("schmerzen").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cPlease execute this command as a player");
            return true;
        }

        if (player.getInventory().getHelmet().getType() == Material.AIR
                || player.getInventory().getHelmet() == null)
            return true;

        ItemStack itemStack = player.getInventory().getHelmet().clone();
        player.getInventory().setHelmet(null);
        player.getInventory().addItem(itemStack);
        return false;
    }
}