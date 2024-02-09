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

public class CommandMute implements CommandExecutor {

    private final WinterVillage plugin;

    public CommandMute(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("mute").setExecutor(this);
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

                if (playerName.equalsIgnoreCase("general")) {
                    this.plugin.CHAT_MUTED = !this.plugin.CHAT_MUTED;
                    Bukkit.broadcastMessage(this.plugin.serverPrefix + "§eDer Chat wurde " + (!this.plugin.CHAT_MUTED ? "§aaktiviert" : "§cdeaktiviert"));
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
                if (target.hasMetadata("MUTE")) {
                    player.sendMessage(this.plugin.serverPrefix + targetUsersRole.color() + targetUser.name() + " §cist bereits stummgeschalten");
                    return true;
                }

                this.plugin.setMetadata(target, "MUTE", true);
                player.sendMessage(this.plugin.serverPrefix + "§7Du hast " + targetUsersRole.color() + targetUser.name() + " §7stummgeschalten");

                target.sendMessage(this.plugin.serverPrefix + "§cDu wurdest stummgeschaltet!");
                target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 1.0f, .5f);
            }

            default -> error(player);
        }

        return false;
    }

    private void error(Player player) {
        player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
        player.sendMessage(this.plugin.serverPrefix + "§a/mute general");
        player.sendMessage(this.plugin.serverPrefix + "§a/mute <Spieler>");
    }
}
