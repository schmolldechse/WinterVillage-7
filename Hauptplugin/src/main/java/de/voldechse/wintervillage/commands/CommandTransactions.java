package de.voldechse.wintervillage.commands;

import de.voldechse.wintervillage.WinterVillage;
import de.voldechse.wintervillage.database.TransactionsDatabase;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class CommandTransactions implements CommandExecutor {

    private final WinterVillage plugin;

    public CommandTransactions(WinterVillage plugin) {
        this.plugin = plugin;
        this.plugin.getInstance().getCommand("transactions").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(this.plugin.serverPrefix + "§cPlease execute this command as a player");
            return true;
        }

        switch (args.length) {
            case 0 -> {
                if (this.plugin.transactionsDatabase.data(player.getUniqueId()).isEmpty()) {
                    player.sendMessage(this.plugin.serverPrefix + "§cEs gibt keine Transaktionen auf deinem Konto");
                    return true;
                }

                player.sendMessage("§8---------- §eTransaktionen §8----------");

                List<TransactionsDatabase.TransactionData> list = this.plugin.transactionsDatabase.data(player.getUniqueId());

                list.forEach(transactionData -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm");

                    if (transactionData.to.equals(player.getUniqueId())) {
                        String who = "";
                        if (transactionData.from.equals(new UUID(0, 0))) {
                            who = "§cBank";
                        } else {
                            PermissionUser fromUser = this.plugin.permissionManagement.userAsync(transactionData.from).join();
                            PermissionGroup fromUsersGroup = this.plugin.permissionManagement.highestPermissionGroup(fromUser);
                            who = fromUsersGroup.color() + fromUser.name();
                        }

                        player.sendMessage("§a\uD83E\uDC16 §e" + plugin.formatBalance(transactionData.amount) + " $ §7von " + who + " §8[§e" + formatter.format(transactionData.date) + "§8]");
                    } else {
                        String who = "";
                        if (transactionData.to.equals(new UUID(0, 0))) {
                            who = "§cBank";
                        } else {
                            PermissionUser toUser = this.plugin.permissionManagement.userAsync(transactionData.to).join();
                            PermissionGroup toUsersGroup = this.plugin.permissionManagement.highestPermissionGroup(toUser);
                            who = toUsersGroup.color() + toUser.name();
                        }

                        player.sendMessage("§c\uD83E\uDC14 §e" + plugin.formatBalance(transactionData.amount) + " $ §7an " + who + " §8[§e" + formatter.format(transactionData.date) + "§8]");
                    }
                });
                player.sendMessage("§8-------------------------");
            }

            case 1 -> {
                if (!player.hasPermission("wintervillage.command.transactions.extended")) {
                    player.sendMessage("§cIm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
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

                PermissionUser permissionUser = this.plugin.permissionManagement.userAsync(fetched).join();
                PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
                if (permissionUser == null || permissionGroup == null) {
                    player.sendMessage(this.plugin.serverPrefix + "§cAn error occurred while searching §b" + permissionUser.name() + "'s §centry in database");
                    return true;
                }

                if (this.plugin.transactionsDatabase.data(fetched).isEmpty()) {
                    player.sendMessage(this.plugin.serverPrefix + "§cEs gibt keine Transaktionen auf dem Konto von " + permissionGroup.color().replace("&", "§") + permissionUser.name());
                    return true;
                }

                player.sendMessage("§8------ §eTransaktionen von §r" + permissionGroup.color().replace("&", "§") + permissionUser.name() + " §8------");

                List<TransactionsDatabase.TransactionData> list = this.plugin.transactionsDatabase.data(fetched);

                list.forEach(transactionData -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm");

                    if (transactionData.to.equals(fetched)) {
                        String who = "";
                        if (transactionData.from.equals(new UUID(0, 0))) {
                            who = "§cBank";
                        } else {
                            PermissionUser fromUser = this.plugin.permissionManagement.userAsync(transactionData.from).join();
                            PermissionGroup fromUsersGroup = this.plugin.permissionManagement.highestPermissionGroup(fromUser);
                            who = fromUsersGroup.color() + fromUser.name();
                        }

                        player.sendMessage("§a\uD83E\uDC16 §e" + plugin.formatBalance(transactionData.amount) + " $ §7von " + who + " §8[§e" + formatter.format(transactionData.date) + "§8]");
                    } else {
                        String who = "";
                        if (transactionData.to.equals(new UUID(0, 0))) {
                            who = "§cBank";
                        } else {
                            PermissionUser toUser = this.plugin.permissionManagement.userAsync(transactionData.to).join();
                            PermissionGroup toUsersGroup = this.plugin.permissionManagement.highestPermissionGroup(toUser);
                            who = toUsersGroup.color() + toUser.name();
                        }

                        player.sendMessage("§c\uD83E\uDC14 §e" + plugin.formatBalance(transactionData.amount) + " $ §7an " + who + " §8[§e" + formatter.format(transactionData.date) + "§8]");
                    }
                });
                player.sendMessage("§8-------------------------");
            }

            default -> {
                error(player);
            }
        }
        return false;
    }

    private void error(Player player) {
        player.sendMessage(this.plugin.serverPrefix + "§cFalsche Syntax! Verwende folgendermaßen:");
        player.sendMessage(this.plugin.serverPrefix + "§a/transactions");
        if (player.hasPermission("wintervillage.proxy.command.transactions.extended"))
            player.sendMessage(this.plugin.serverPrefix + "§a/transactions <Spieler>");
    }
}