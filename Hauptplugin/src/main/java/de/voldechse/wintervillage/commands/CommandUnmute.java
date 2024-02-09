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

public class CommandUnmute implements CommandExecutor {

    private final WinterVillage plugin;

    public CommandUnmute(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("unmute").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cPlease execute this command as a player");
            return true;
        }

        switch (args.length) {
            case 1 -> {
                String playerName = args[0];
                UUID fetched = this.plugin.mojangAPIFetcher.uuidFrom(playerName);
                if (fetched == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while fetching the uuid from §b" + playerName);
                    return true;
                }

                if (!this.plugin.permissionManagement.containsUserAsync(fetched).join()) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
                    return true;
                }
                PermissionUser permissionUser = this.plugin.permissionManagement.userAsync(fetched).join();
                PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
                if (permissionUser == null || permissionGroup == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching players entry in database");
                    return true;
                }

                if (Bukkit.getPlayer(fetched) == null) {
                    player.sendMessage(this.plugin.serverPrefix + permissionGroup.color() + permissionUser.name() + " §cist nicht online");
                    return true;
                }

                Player target = Bukkit.getPlayer(fetched);
                if (!target.hasMetadata("MUTE")) {
                    player.sendMessage(this.plugin.serverPrefix + permissionGroup.color() + permissionUser.name() + " §cist nicht stummgeschalten");
                    return true;
                }

                this.plugin.removeMetadata(target, "MUTE");
                player.sendMessage(this.plugin.serverPrefix + "§7Du hast die Stummschaltung von " + permissionGroup.color() + permissionUser.name() + " §7aufgehoben");
            }
        }

        return false;
    }
}
