package de.voldechse.wintervillage.commands;

import de.voldechse.wintervillage.WinterVillage;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CommandInventory implements CommandExecutor {

    private final WinterVillage plugin;

    public CommandInventory(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("inventory").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cPlease execute this command as a player");
            return true;
        }

        switch (args.length) {
            case 2 -> {
                String playerName = args[1];
                UUID fetched = this.plugin.mojangAPIFetcher.uuidFrom(playerName);
                if (fetched == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while fetching the uuid from §b" + playerName);
                    return true;
                }

                if (player.getUniqueId().equals(fetched)) {
                    player.sendMessage(this.plugin.serverPrefix + "§cDu kannst nicht dein eigenes Inventar nochmal öffnen");
                    return true;
                }

                if (!this.plugin.permissionManagement.containsUserAsync(fetched).join()) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + playerName + "'s §centry in database");
                    return true;
                }
                PermissionUser permissionUser = this.plugin.permissionManagement.userAsync(fetched).join();
                PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
                if (permissionUser == null || permissionGroup == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + playerName + "'s §centry in database");
                    return true;
                }

                if (Bukkit.getPlayer(fetched) == null) {
                    player.sendMessage(this.plugin.serverPrefix + permissionGroup.color().replace("&", "§") + permissionUser.name() + " §cist nicht online");
                    return true;
                }

                switch (args[0].toUpperCase()) {
                    case "PLAYER" -> {
                        player.openInventory(Bukkit.getPlayer(fetched).getInventory());
                        player.playSound(player.getLocation(), Sound.ENTITY_HORSE_SADDLE, 1.0f, 1.0f);
                    }

                    case "ENDER_CHEST", "EC" -> {
                        player.openInventory(Bukkit.getPlayer(fetched).getEnderChest());
                        player.playSound(player.getLocation(), Sound.ENTITY_HORSE_SADDLE, 1.0f, 1.0f);
                    }

                    default -> error(player);
                }
            }

            default -> error(player);
        }

        return true;
    }

    private void error(Player player) {
        player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
        player.sendMessage(this.plugin.serverPrefix + "§a/inventory <PLAYER/ENDER_CHEST> <Spieler>");
    }
}
