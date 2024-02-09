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

public class CommandFreeze implements CommandExecutor {

    private final WinterVillage plugin;

    public CommandFreeze(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("freeze").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cPlease execute this command as a player");
            return true;
        }

        if (!this.plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join()) {
            player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
            return true;
        }
        PermissionUser permissionUser = this.plugin.permissionManagement.userAsync(player.getUniqueId()).join();
        PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionUser == null || permissionGroup == null) {
            player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
            return true;
        }

        switch (args.length) {
            case 1 -> {
                String playerName = args[0];

                if (playerName.equalsIgnoreCase("everyone")) {
                    this.plugin.PLAYERS_FREEZED = !this.plugin.PLAYERS_FREEZED;

                    if (!this.plugin.PLAYERS_FREEZED)
                        Bukkit.broadcastMessage(this.plugin.serverPrefix + "§eAlle Spieler wurden wieder aufgetaut");
                    else Bukkit.broadcastMessage(this.plugin.serverPrefix + "§cAlle Spieler wurden eingefroren");

                    Bukkit.getOnlinePlayers().forEach(online -> online.playSound(online.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 1.0f, .5f));
                    return true;
                }

                UUID fetched = this.plugin.mojangAPIFetcher.uuidFrom(playerName);
                if (fetched == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while fetching the uuid from §b" + playerName);
                    return true;
                }

                if (!this.plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join()) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching players entry in database");
                    return true;
                }
                PermissionUser targetUser = this.plugin.permissionManagement.userAsync(fetched).join();
                PermissionGroup targetUsersRole = this.plugin.permissionManagement.highestPermissionGroup(targetUser);
                if (targetUser == null || targetUsersRole == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching players entry in database");
                    return true;
                }

                if (Bukkit.getPlayer(fetched) == null) {
                    player.sendMessage(this.plugin.serverPrefix + targetUsersRole.color() + targetUser.name() + " §cist nicht online");
                    return true;
                }

                Player target = Bukkit.getPlayer(fetched);
                if (target.hasMetadata("FREEZED")) {
                    this.plugin.removeMetadata(target, "FREEZED");

                    target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 1.0f, .5f);
                    target.sendMessage(this.plugin.serverPrefix + "§eDu bist nun wieder bewegungsfähig");
                    player.sendMessage(this.plugin.serverPrefix + targetUsersRole.color() + targetUser.name() + " §fkann sich nun wieder bewegen");
                    return true;
                }

                this.plugin.setMetadata(target, "FREEZED", true);
                target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 1.0f, .5f);
                target.sendMessage(this.plugin.serverPrefix + "§cDu wurdest eingefroren!");
                player.sendMessage(this.plugin.serverPrefix + targetUsersRole.color() + targetUser.name() + " §fist nun eingefroren");
                return true;
            }

            default -> error(player);
        }

        return false;
    }

    private void error(Player player) {
        player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
        player.sendMessage(this.plugin.serverPrefix + "§a/freeze everyone");
        player.sendMessage(this.plugin.serverPrefix + "§a/freeze <Spieler>");
    }
}
