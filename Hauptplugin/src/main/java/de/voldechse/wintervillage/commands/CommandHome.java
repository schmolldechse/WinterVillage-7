package de.voldechse.wintervillage.commands;

import de.voldechse.wintervillage.WinterVillage;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CommandHome implements CommandExecutor {

    private final WinterVillage plugin;

    public CommandHome(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("home").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cPlease execute this command as a player");
            return true;
        }

        switch (args.length) {
            case 0 -> {
                if (!this.plugin.homeDatabase.saved(player.getUniqueId())) {
                    player.sendMessage(this.plugin.serverPrefix + "§cDu hast aktuell noch kein Zuhause gespeichert");
                    return true;
                }

                player.teleport(this.plugin.homeDatabase.home(player.getUniqueId()));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            }

            case 1 -> {
                String playerName = args[0];
                UUID fetched = this.plugin.mojangAPIFetcher.uuidFrom(playerName);
                if (fetched == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while fetching the uuid from §b" + playerName);
                    return true;
                }

                if (!this.plugin.permissionManagement.containsUserAsync(fetched).join()) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching the shop owner's entry in database");
                    return true;
                }

                PermissionUser permissionUser = this.plugin.permissionManagement.userAsync(fetched).join();
                PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
                if (permissionUser == null || permissionGroup == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + playerName + "'s entry in database");
                    return true;
                }

                if (!this.plugin.homeDatabase.saved(fetched)) {
                    player.sendMessage(this.plugin.serverPrefix + permissionGroup.color() + permissionUser.name() + " §chat sein Zuhause noch nicht gespeichert");
                    return true;
                }

                player.teleport(this.plugin.homeDatabase.home(fetched));
                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

                player.sendMessage(this.plugin.serverPrefix + "§fDu wurdest zum Zuhause " + permissionGroup.color() + permissionUser.name() + " §fteleportiert");
            }

            default -> error(player);
        }

        return true;
    }

    private void error(Player player) {
        player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
        player.sendMessage(this.plugin.serverPrefix + "§a/home (Spieler)");
    }
}
