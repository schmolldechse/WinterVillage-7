package de.voldechse.wintervillage.commands;

import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.database.WhitelistDatabase;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class CommandFly implements CommandExecutor {

    private final LocalDateTime CHECK_WHITELISTED, CHECK_STARTED, CHECK_ENDED;

    private final WinterVillage plugin;

    public CommandFly(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("fly").setExecutor(this);

        this.CHECK_WHITELISTED = LocalDateTime.of(2023, 12, 17, 0, 0);
        this.CHECK_STARTED = LocalDateTime.of(2023, 12, 17, 15, 0);
        this.CHECK_ENDED = LocalDateTime.of(2023, 12, 18, 15, 0);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cPlease execute this command as a player");
            return true;
        }

        PermissionUser permissionUser = this.plugin.permissionManagement.getOrCreateUserAsync(player.getUniqueId(), player.getName()).join();
        if (permissionUser == null) {
            player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
            return true;
        }
        PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) {
            player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
            return true;
        }

        if (permissionGroup.potency() == this.plugin.potencySpectator) {
            player.sendMessage(this.plugin.serverPrefix + "§cDu bist dazu nicht berechtigt");
            return true;
        }

        LocalDateTime current = LocalDateTime.now();

        switch (args.length) {
            case 0 -> {
                if (permissionGroup.potency() < this.plugin.potencyAdmin) {
                    if (!this.plugin.whitelistDatabase.whitelisted(player.getUniqueId())) {
                        player.sendMessage(this.plugin.serverPrefix + "§cNur Spieler, welche vor dem 17.12 zur Whitelist hinzugefügt worden sind, sind dazu berechtigt");
                        return true;
                    }

                    WhitelistDatabase.WhitelistData whitelist = this.plugin.whitelistDatabase.data(player.getUniqueId());
                    if (whitelist.date.isAfter(this.CHECK_WHITELISTED)) {
                        player.sendMessage(this.plugin.serverPrefix + "§cNur Spieler, welche vor dem 17.12 zur Whitelist hinzugefügt worden sind, sind dazu berechtigt");
                        return true;
                    }

                    if (current.isBefore(this.CHECK_STARTED)) {
                        player.sendMessage(this.plugin.serverPrefix + "§cDiese Aktion startet erst ab dem 17.12 um 15 Uhr");
                        return true;
                    }

                    if (current.isAfter(this.CHECK_ENDED)) {
                        player.sendMessage(this.plugin.serverPrefix + "§cDiese Aktion ging nur bis zum 18.12 bis 15 Uhr");
                        return true;
                    }

                    if (!player.getWorld().getName().equalsIgnoreCase("world")) {
                        player.sendMessage(this.plugin.serverPrefix + "§cDu bist nicht dazu berechtigt, diesen Befehl in dieser Welt auszuführen");
                        return true;
                    }

                    if (player.hasMetadata("FLYING")) {
                        this.plugin.removeMetadata(player, "FLYING");

                        player.setAllowFlight(false);
                        player.setFlying(false);

                        player.sendMessage(this.plugin.serverPrefix + "§eDu kannst nun nicht mehr fliegen");
                        return true;
                    }

                    this.plugin.setMetadata(player, "FLYING", true);

                    player.setAllowFlight(true);
                    player.setFlying(true);

                    player.sendMessage(this.plugin.serverPrefix + "§eDu kannst nun fliegen");
                    return true;
                }

                if (player.hasMetadata("FLYING")) {
                    this.plugin.removeMetadata(player, "FLYING");

                    player.setAllowFlight(false);
                    player.setFlying(false);

                    player.sendMessage(this.plugin.serverPrefix + "§eDu kannst nun nicht mehr fliegen");
                    return true;
                }
                this.plugin.setMetadata(player, "FLYING", true);

                player.setAllowFlight(true);
                player.setFlying(true);

                player.sendMessage(this.plugin.serverPrefix + "§eDu kannst nun fliegen");
                return true;
            }

            case 1 -> {
                if (!player.hasPermission("wintervillage.command.fly.extended")) {
                    player.sendMessage("§cIm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
                    return true;
                }

                String playerName = args[0];

                UUID fetched = this.plugin.mojangAPIFetcher.uuidFrom(playerName);
                if (fetched == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while fetching the uuid from §b" + playerName);
                    return true;
                }

                PermissionUser targetUser = this.plugin.permissionManagement.getOrCreateUserAsync(fetched, playerName).join();
                if (targetUser == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + playerName + "§c's entry in database");
                    return true;
                }
                PermissionGroup targetUsersRole = this.plugin.permissionManagement.highestPermissionGroup(targetUser);
                if (targetUsersRole == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + targetUser.name() + "§c's entry in database");
                    return true;
                }

                if (Bukkit.getPlayer(fetched) == null) {
                    player.sendMessage(this.plugin.serverPrefix + targetUsersRole.color() + targetUser.name() + " §cist nicht online");
                    return true;
                }

                Player target = Bukkit.getPlayer(fetched);

                if (target.hasMetadata("FLYING")) {
                    this.plugin.removeMetadata(target, "FLYING");

                    target.setAllowFlight(false);
                    target.setFlying(false);

                    target.sendMessage(this.plugin.serverPrefix + "§eDu kannst nun nicht mehr fliegen");
                    player.sendMessage(this.plugin.serverPrefix + targetUsersRole.color() + targetUser.name() + " §ekann nun nicht mehr fliegen");
                    return true;
                }

                this.plugin.setMetadata(target, "FLYING", true);

                target.setAllowFlight(true);
                target.setFlying(true);

                target.sendMessage(this.plugin.serverPrefix + "§eDu kannst nun fliegen");
                player.sendMessage(this.plugin.serverPrefix + targetUsersRole.color() + targetUser.name() + " §ekann nun fliegen");
            }

            default -> error(player);
        }

        return false;
    }

    private void error(Player player) {
        player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
        player.sendMessage(this.plugin.serverPrefix + "§a/fly" + (player.hasPermission("wintervillage.command.fly.extended") ? " (Spieler)" : ""));
    }
}
