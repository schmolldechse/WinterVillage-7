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
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CommandTpaccept implements CommandExecutor {

    private final WinterVillage plugin;

    public CommandTpaccept(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("tpaccept").setExecutor(this);
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
                if (!CommandTPA.tpaRequest.containsKey(player.getUniqueId())) {
                    player.sendMessage(this.plugin.serverPrefix + "§cDu hast keine offenen Teleportations-Anfragen");
                    return true;
                }

                String playerName = args[0];
                UUID fetched = this.plugin.mojangAPIFetcher.uuidFrom(playerName);
                if (fetched == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while fetching the uuid from §b" + playerName);
                    return true;
                }

                if (!this.plugin.permissionManagement.containsUserAsync(fetched).join()) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + playerName + "'s §centry in database");
                    return true;
                }
                PermissionUser targetUser = this.plugin.permissionManagement.userAsync(fetched).join();
                PermissionGroup targetUsersRole = this.plugin.permissionManagement.highestPermissionGroup(targetUser);
                if (targetUser == null || targetUsersRole == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + playerName + "'s §centry in database");
                    return true;
                }

                if (Bukkit.getPlayer(fetched) == null) {
                    player.sendMessage(this.plugin.serverPrefix + targetUsersRole.color() + targetUser.name() + " §cist nicht online");
                    return true;
                }

                Player target = Bukkit.getPlayer(fetched);

                if (!CommandTPA.tpaRequest.get(player.getUniqueId()).equals(target.getUniqueId())) {
                    player.sendMessage(this.plugin.serverPrefix + targetUsersRole.color() + targetUser.name() + " §chat dir keine Teleportations-Anfrage geschickt");
                    return true;
                }

                for (UUID sendFrom : CommandTPA.tpaRequest.values()) {
                    if (!(sendFrom.equals(target.getUniqueId()) && CommandTPA.tpaRequest.get(player.getUniqueId()).equals(sendFrom))) continue;

                    CommandTPA.tpaRequest.remove(player.getUniqueId(), sendFrom);

                    schedule(player, target, permissionUser, permissionGroup);

                    player.sendMessage(this.plugin.serverPrefix + "§fIn §e10 Sekunden §fwird " + targetUsersRole.color() + targetUser.name() + " §fzu dir teleportiert");
                    target.sendMessage(this.plugin.serverPrefix + "§fIn §e10 Sekunden §fwirst du zu " + permissionGroup.color() + permissionUser.name() + " §fteleportiert");
                    return true;
                }

                player.sendMessage(this.plugin.serverPrefix + "§c?");
            }

            default -> error(player);
        }

        return false;
    }

    private void schedule(Player player, Player target, PermissionUser permissionUser, PermissionGroup permissionGroup) {
        new BukkitRunnable() {
            @Override
            public void run() {
                target.sendMessage(plugin.serverPrefix + "§fDu wurdest zu " + permissionGroup.color().replace("&", "§") + permissionUser.name() + " §fteleportiert");
                target.teleport(player);
                target.playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, .5f);
            }
        }.runTaskLater(this.plugin.getInstance(), 10 * 20L);
    }

    private void error(Player player) {
        player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
        player.sendMessage(this.plugin.serverPrefix + "§a/tpaccept <Spieler>");
    }
}
