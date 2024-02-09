package de.voldechse.wintervillage.commands;

import de.voldechse.wintervillage.WinterVillage;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CommandBalance implements CommandExecutor {

    private final WinterVillage plugin;

    public CommandBalance(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("balance").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cPlease execute this command as a player");
            return true;
        }

        switch (args.length) {
            case 0 -> {
                if (!this.plugin.balanceDatabase.saved(player.getUniqueId())) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
                    return true;
                }

                player.sendMessage(this.plugin.serverPrefix + "§7Dein Kontostand§8: §e" + this.plugin.formatBalance(this.plugin.balanceDatabase.balance(player.getUniqueId())) + " $");
            }

            case 1 -> {
                if (!player.hasPermission("wintervillage.command.balance.extended")) {
                    player.sendMessage("§cIm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
                    return true;
                }

                String playerName = args[0];
                UUID fetched = this.plugin.mojangAPIFetcher.uuidFrom(playerName);
                if (fetched == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while fetching the uuid from §b" + playerName);
                    return true;
                }

                if (!this.plugin.balanceDatabase.saved(fetched)) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + playerName + "'s §centry in database");
                    return true;
                }

                PermissionUser permissionUser = this.plugin.permissionManagement.getOrCreateUserAsync(fetched, playerName).join();
                if (permissionUser == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + playerName + "§c's entry in database");
                    return true;
                }
                PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
                if (permissionGroup == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + permissionUser.name() + "§c's entry in database");
                    return true;
                }

                player.sendMessage(this.plugin.serverPrefix + permissionGroup.color() + permissionUser.name() + "§7's Kontostand§8: §e" + this.plugin.formatBalance(this.plugin.balanceDatabase.balance(fetched)) + " $");
            }

            default -> error(player);
        }

        return false;
    }

    private void error(Player player) {
        player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
        player.sendMessage(this.plugin.serverPrefix + "§a/balance (Spieler)");
    }
}