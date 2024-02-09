package de.voldechse.wintervillage.commands;

import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.database.TransactionsDatabase;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class CommandTransfer implements CommandExecutor {

    private final WinterVillage plugin;

    public CommandTransfer(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("transfer").setExecutor(this);
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
            case 2 -> {
                String playerName = args[0];
                UUID fetched = this.plugin.mojangAPIFetcher.uuidFrom(playerName);
                if (fetched == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while fetching the uuid from §b" + playerName);
                    return true;
                }

                if (playerName.equals(player.getName())) {
                    player.sendMessage(this.plugin.serverPrefix + "§cDu kannst dir selbst kein Geld überweisen");
                    return true;
                }

                if (!this.plugin.balanceDatabase.saved(fetched)) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching players entry in database");
                    return true;
                }

                if (!this.plugin.permissionManagement.containsUserAsync(fetched).join()) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + playerName + "'s entry in database");
                    return true;
                }
                PermissionUser sendTo = this.plugin.permissionManagement.userAsync(fetched).join();
                PermissionGroup sendToRole = this.plugin.permissionManagement.highestPermissionGroup(sendTo);
                if (sendTo == null || sendToRole == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + sendTo.name() + "'s entry in database");
                    return true;
                }

                BigDecimal bigDecimal = BigDecimal.valueOf(0);
                try {
                    bigDecimal = new BigDecimal(args[1]);
                } catch (NumberFormatException exception) {
                    player.sendMessage(this.plugin.serverPrefix + "§cUngültige Zahl");
                }

                if (this.plugin.balanceDatabase.balance(player.getUniqueId()).compareTo(bigDecimal) < 0) {
                    player.sendMessage(this.plugin.serverPrefix + "§cDu hast zu wenig Geld auf deinem Konto");
                    return true;
                }

                if (bigDecimal.compareTo(BigDecimal.ZERO) <= 0) {
                    player.sendMessage(this.plugin.serverPrefix + "§cDu kannst nicht weniger als 0 $ überweisen");
                    return true;
                }

                this.plugin.transactionsDatabase.save(new TransactionsDatabase.TransactionData(
                        player.getUniqueId(),
                        fetched,
                        bigDecimal,
                        LocalDateTime.now().plusHours(1)
                ));

                this.plugin.balanceDatabase.modify(player.getUniqueId(), bigDecimal.negate());
                this.plugin.balanceDatabase.modify(fetched, bigDecimal);

                this.plugin.scoreboardManager.updateScoreboard(player, "currentBalance", " §e" + plugin.formatBalance(plugin.balanceDatabase.balance(player.getUniqueId())) + " $", "");
                player.sendMessage(this.plugin.serverPrefix + "§7Du hast " + sendToRole.color() + sendTo.name() + " §e" + this.plugin.formatBalance(bigDecimal) + " $ §7überwiesen");

                if (Bukkit.getPlayer(fetched) != null) {
                    Player target = Bukkit.getPlayer(fetched);

                    this.plugin.scoreboardManager.updateScoreboard(target, "currentBalance", " §e" + plugin.formatBalance(plugin.balanceDatabase.balance(fetched)) + " $", "");
                    target.sendMessage(this.plugin.serverPrefix + permissionGroup.color() + permissionUser.name() + " §7hat dir §e" + this.plugin.formatBalance(bigDecimal) + " $ §7überwiesen");
                }
            }

            case 3 -> {
                if (!player.hasPermission("wintervillage.command.transfer.extended")) {
                    player.sendMessage("§cIm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
                    return true;
                }

                if (!args[0].equalsIgnoreCase("admin")) {
                    error(player);
                    return true;
                }

                String playerName = args[1];
                UUID fetched = this.plugin.mojangAPIFetcher.uuidFrom(playerName);
                if (fetched == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while fetching the uuid from §b" + playerName);
                    return true;
                }

                if (!this.plugin.balanceDatabase.saved(fetched)) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching players entry in database");
                    return true;
                }

                if (!this.plugin.permissionManagement.containsUserAsync(fetched).join()) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + playerName + "'s entry in database");
                    return true;
                }

                PermissionUser sendTo = this.plugin.permissionManagement.userAsync(fetched).join();
                PermissionGroup sendToRole = this.plugin.permissionManagement.highestPermissionGroup(sendTo);
                if (sendTo == null || sendToRole == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + sendTo.name() + "'s entry in database");
                    return true;
                }

                BigDecimal bigDecimal = BigDecimal.valueOf(0);
                try {
                    bigDecimal = new BigDecimal(args[2]);
                } catch (NumberFormatException exception) {
                    player.sendMessage(this.plugin.serverPrefix + "§cUngültige Zahl");
                }

                if (bigDecimal.compareTo(BigDecimal.ZERO) <= 0) {
                    player.sendMessage(this.plugin.serverPrefix + "§cDu kannst nicht weniger als 0 $ überweisen");
                    return true;
                }

                this.plugin.transactionsDatabase.save(new TransactionsDatabase.TransactionData(
                        new UUID(0, 0),
                        fetched,
                        bigDecimal,
                        LocalDateTime.now().plusHours(1)
                ));

                this.plugin.balanceDatabase.modify(fetched, bigDecimal);

                player.sendMessage(this.plugin.serverPrefix + "§cBank §7hat " + sendToRole.color() + sendTo.name() + " §e" + this.plugin.formatBalance(bigDecimal) + " $ §7überwiesen");

                if (Bukkit.getPlayer(fetched) != null) {
                    Player target = Bukkit.getPlayer(fetched);
                    target.sendMessage(this.plugin.serverPrefix + "§cBank §7hat dir §e" + this.plugin.formatBalance(bigDecimal) + " $ §7überwiesen");
                    this.plugin.scoreboardManager.updateScoreboard(target, "currentBalance", " §e" + plugin.formatBalance(plugin.balanceDatabase.balance(fetched)) + " $", "");
                }
            }

            default -> {
                error(player);
            }
        }
        return false;
    }

    private void error(Player player) {
        player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
        player.sendMessage(this.plugin.serverPrefix + "§a/transfer <Spieler> <Betrag>");
        if (player.hasPermission("wintervillage.command.transfer.extended"))
            player.sendMessage(this.plugin.serverPrefix + "§a/transfer admin <Spieler> <Betrag>");
    }
}