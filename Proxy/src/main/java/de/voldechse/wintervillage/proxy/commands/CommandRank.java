package de.voldechse.wintervillage.proxy.commands;

import de.voldechse.wintervillage.proxy.Proxy;
import dev.derklaro.aerogel.Inject;
import dev.derklaro.aerogel.Singleton;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

@Singleton
public class CommandRank extends Command {

    private final Proxy plugin;

    @Inject
    public CommandRank(Proxy plugin) {
        super("rank", "wintervillage.proxy.command.rank", "rang");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof ProxiedPlayer player)) {
            commandSender.sendMessage(new ComponentBuilder("§cPlease execute this command as a player").create());
            return;
        }

        if (!this.plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join()) {
            player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while searching your entry in database").create());
            return;
        }

        switch (args.length) {
            case 1 -> {
                if (args[0].equalsIgnoreCase("list")) {
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fEs existieren diese Ränge:").create());

                    StringBuilder ranks = new StringBuilder();
                    for (PermissionGroup permissionGroup : this.plugin.permissionManagement.groupsAsync().join()) {
                        if (ranks.length() > 1) ranks.append("§8, ");
                        ranks.append(permissionGroup.color().replace("&", "§")).append(permissionGroup.name());
                    }

                    player.sendMessage(new ComponentBuilder(" " + ranks.toString()).create());
                    return;
                }

                String playerName = args[0];
                UUID fetched = this.plugin.mojangAPIFetcher.uuidFrom(playerName);
                if (fetched == null) {
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while fetching the uuid from §b" + playerName).create());
                    return;
                }

                PermissionUser permissionUser = this.plugin.permissionManagement.getOrCreateUserAsync(fetched, playerName).join();
                if (permissionUser == null) {
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while searching §b" + playerName + "§c's entry in database").create());
                    return;
                }

                player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§e" + fetched + " §8[§a" + playerName + "§8]").create());
                player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fRänge§8:").create());

                permissionUser.groups().forEach(permissionUserGroupInfo -> {
                    PermissionGroup permissionGroup = this.plugin.permissionManagement.groupAsync(permissionUserGroupInfo.group()).join();
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§8- " + permissionGroup.color().replace("&", "§") + permissionGroup.name()).create());
                });
            }

            case 3 -> {
                String playerName = args[1];
                UUID fetched = this.plugin.mojangAPIFetcher.uuidFrom(playerName);
                if (fetched == null) {
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while fetching the uuid from §b" + playerName).create());
                    return;
                }

                String role = args[2];
                if (!this.plugin.permissionManagement.containsGroupAsync(role).join()) {
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§b" + role + " §ckonnte nicht in der Datenbank gefunden werden. Es existieren diese Ränge:").create());

                    this.plugin.permissionManagement.groupsAsync().join().forEach(permissionGroup -> {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§8- " + permissionGroup.color().replace("&", "§") + permissionGroup.name()).create());
                    });

                    return;
                }

                if (args[0].equalsIgnoreCase("set")) {
                    if (!this.plugin.permissionManagement.containsUserAsync(fetched).join())
                        this.plugin.permissionManagement.getOrCreateUserAsync(fetched, playerName).join();
                    PermissionGroup permissionGroup = this.plugin.permissionManagement.groupAsync(role).join();

                    PermissionUser permissionUser = this.plugin.permissionManagement.userAsync(fetched).join();
                    permissionUser.groups().clear();
                    permissionUser.addGroup(permissionGroup.name());
                    this.plugin.permissionManagement.updateUserAsync(permissionUser).join();

                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fDer Spieler §a" + playerName + " §8[§e" + fetched + "§8] §fbesitzt nun den Rang " + permissionGroup.color().replace("&", "§") + permissionGroup.name()).create());
                    return;
                }

                if (args[0].equalsIgnoreCase("add")) {
                    if (!this.plugin.permissionManagement.containsUserAsync(fetched).join())
                        this.plugin.permissionManagement.getOrCreateUserAsync(fetched, playerName).join();
                    PermissionGroup permissionGroup = this.plugin.permissionManagement.groupAsync(role).join();

                    PermissionUser permissionUser = this.plugin.permissionManagement.userAsync(fetched).join();
                    if (permissionUser.inGroup(permissionGroup.name())) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cDer Spieler besitzt bereits den Rang §r" + permissionGroup.color().replace("&", "§") + permissionGroup.name()).create());
                        return;
                    }

                    permissionUser.addGroup(permissionGroup.name());
                    this.plugin.permissionManagement.updateUserAsync(permissionUser).join();

                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fDem Spieler §a" + playerName + " §8[§e" + fetched + "§8] §fwurde der Rang " + permissionGroup.color().replace("&", "§") + permissionGroup.name() + " §fhinzugefügt").create());
                    return;
                }

                if (args[0].equalsIgnoreCase("remove")) {
                    if (!this.plugin.permissionManagement.containsUserAsync(fetched).join()) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cDer Spieler §b" + playerName + " §cist nicht in der Datenbank gespeichert").create());
                        return;
                    }
                    PermissionGroup permissionGroup = this.plugin.permissionManagement.groupAsync(role).join();

                    PermissionUser permissionUser = this.plugin.permissionManagement.getOrCreateUserAsync(fetched, playerName).join();
                    if (permissionUser == null) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while searching §b" + playerName + "§c's entry in database").create());
                        return;
                    }

                    if (!permissionUser.inGroup(permissionGroup.name())) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cDer Spieler besitzt nicht den Rang §r" + permissionGroup.color().replace("&", "§") + permissionGroup.name()).create());
                        return;
                    }

                    permissionUser.removeGroup(permissionGroup.name());
                    this.plugin.permissionManagement.updateUserAsync(permissionUser).join();

                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fDem Spieler §a" + playerName + " §8[§e" + fetched + "§8] §fwurde der Rang " + permissionGroup.color().replace("&", "§") + permissionGroup.name() + " §fentzogen").create());
                    return;
                }

                error(player);
            }

            default -> {
                error(player);
            }
        }
    }

    private void error(ProxiedPlayer player) {
        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cFalsche Syntax! Verwende folgendermaßen:").create());
        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§a/rank list").create());
        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§a/rank <Spieler>").create());
        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§a/rank add/set/remove <Spieler> <Rang>").create());
    }
}