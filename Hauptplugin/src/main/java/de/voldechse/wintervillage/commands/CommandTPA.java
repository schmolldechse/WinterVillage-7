package de.voldechse.wintervillage.commands;

import de.voldechse.wintervillage.WinterVillage;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandTPA implements CommandExecutor {

    private final WinterVillage plugin;

    public static Map<UUID, UUID> tpaRequest;

    public CommandTPA(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("tpa").setExecutor(this);

        tpaRequest = new HashMap<>();
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
        if (permissionUser == null) {
            player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
            return true;
        }
        PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) {
            player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
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

                if (player.getUniqueId().equals(fetched)) {
                    player.sendMessage(this.plugin.serverPrefix + "§cDu kannst dir keine Teleportations-Anfrage schicken");
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
                //this.plugin.setMetadata(target, "TPA_REQUEST", player.getUniqueId());
                tpaRequest.put(fetched, player.getUniqueId());

                player.sendMessage(this.plugin.serverPrefix + "§fDu hast " + targetUsersRole.color() + targetUser.name() + " §feine Teleport-Anfrage geschickt");
                player.sendMessage(this.plugin.serverPrefix + "§fSie verschwindet nach §c60 Sekunden§f, sollte er sie nicht annehmen");

                target.sendMessage(this.plugin.serverPrefix + permissionGroup.color() + permissionUser.name() + " §fhat dir eine Teleportations-Anfrage geschickt");
                target.sendMessage(this.plugin.serverPrefix + "§fDu hast §c60 Sekunden §fZeit, sie mit §a/tpaccept " + permissionGroup.color() + permissionUser.name() + " §fanzunehmen");

                schedule(target);
            }

            default -> error(player);
        }

        return false;
    }

    private void schedule(Player player) {
        BukkitRunnable later = new BukkitRunnable() {
            @Override
            public void run() {
                if (tpaRequest.containsKey(player.getUniqueId())) {
                    UUID sendFrom = tpaRequest.get(player.getUniqueId());
                    if (Bukkit.getPlayer(sendFrom) == null) {
                        cancel();
                        return;
                    }

                    if (!plugin.permissionManagement.containsUserAsync(sendFrom).join()) {
                        cancel();
                        return;
                    }
                    PermissionUser sendFromUser = plugin.permissionManagement.userAsync(sendFrom).join();
                    PermissionGroup sendFromUsersRole = plugin.permissionManagement.highestPermissionGroup(sendFromUser);
                    if (sendFromUser == null || sendFromUsersRole == null) {
                        cancel();
                        return;
                    }

                    if (!plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join()) {
                        cancel();
                        return;
                    }
                    PermissionUser sendTo = plugin.permissionManagement.userAsync(player.getUniqueId()).join();
                    PermissionGroup sendTosRole = plugin.permissionManagement.highestPermissionGroup(sendTo);
                    if (sendTo == null || sendTosRole == null) {
                        cancel();
                        return;
                    }

                    player.sendMessage(plugin.serverPrefix + "§fDie Teleportations-Anfrage von " + sendFromUsersRole.color().replace("&", "§") + sendFromUser.name() + " §fist abgelaufen");
                    Bukkit.getPlayer(sendFrom).sendMessage(plugin.serverPrefix + "§fDeine Teleportations-Anfrage an " + sendTosRole.color().replace("&", "§") + sendTo.name() + " §fist abgelaufen");

                    tpaRequest.remove(player.getUniqueId());
                    cancel();
                }
            }
        };
        later.runTaskLater(this.plugin.getInstance(), 60 * 20L);
    }

    private void error(Player player) {
        player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
        player.sendMessage(this.plugin.serverPrefix + "§a/tpa <Spieler>");
    }
}
