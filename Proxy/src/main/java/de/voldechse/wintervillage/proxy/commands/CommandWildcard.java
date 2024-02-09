package de.voldechse.wintervillage.proxy.commands;

import de.voldechse.wintervillage.proxy.Proxy;
import de.voldechse.wintervillage.proxy.wildcard.WhitelistDatabase;
import de.voldechse.wintervillage.proxy.wildcard.WildcardDatabase;
import dev.derklaro.aerogel.Singleton;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Singleton
public class CommandWildcard extends Command {

    private final Proxy plugin;

    private final DateTimeFormatter formatter;

    public CommandWildcard(Proxy plugin) {
        super("wildcard", "wintervillage.proxy.command.wildcard");
        this.plugin = plugin;

        this.formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm");
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof ProxiedPlayer player)) {
            commandSender.sendMessage(new ComponentBuilder("§cPlease execute this command as a player").create());
            return;
        }

        switch (args.length) {
            case 0 -> {
                if (!this.plugin.wildcardDatabase.saved(player.getUniqueId())) {
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while searching your entry in database").create());
                    return;
                }

                WildcardDatabase.WildcardData wildcardData = this.plugin.wildcardDatabase.data(player.getUniqueId());

                player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fDu kannst noch §e" + wildcardData.amount + " §fWildcards vergeben").create());
                player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fLetzter Reset§8: §a" + this.formatter.format(wildcardData.lastReset)).create());

                List<WhitelistDatabase.WhitelistData> list = this.plugin.whitelistDatabase.dataList(player.getUniqueId());
                if (!list.isEmpty()) {
                    player.sendMessage(new ComponentBuilder().create());
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fDu hast §a" + list.size() + " §fSpieler zur Whitelist hinzugefügt").create());

                    StringBuilder users = new StringBuilder();
                    for (WhitelistDatabase.WhitelistData whitelistData : list) {
                        if (!this.plugin.permissionManagement.containsUserAsync(whitelistData.receiver).join()) continue;

                        PermissionUser whitelistedUser = this.plugin.permissionManagement.userAsync(whitelistData.receiver).join();
                        if (whitelistedUser == null) continue;

                        PermissionGroup whitelistedUsersGroup = this.plugin.permissionManagement.highestPermissionGroup(whitelistedUser);
                        if (whitelistedUsersGroup == null) continue;

                        if (users.length() > 1) users.append("§8, ");

                        users.append(whitelistedUsersGroup.color()).append(whitelistedUser.name());
                    }

                    player.sendMessage(new ComponentBuilder(" " + users.toString()).create());
                }


                return;
            }

            case 1 -> {
                if (!player.hasPermission("wintervillage.proxy.command.wildcard.extended")) {
                    player.sendMessage(new ComponentBuilder("§cYou do not have permission to execute this command!").create());
                    return;
                }

                String playerName = args[0];
                UUID fetched = this.plugin.mojangAPIFetcher.uuidFrom(playerName);
                if (fetched == null) {
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while fetching the uuid from §b" + playerName).create());
                    return;
                }

                if (!this.plugin.permissionManagement.containsUserAsync(fetched).join()) {
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while searching §b" + playerName + "'s §centry in database").create());
                    return;
                }
                PermissionUser permissionUser = this.plugin.permissionManagement.user(fetched);
                if (permissionUser == null) {
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while searching §b" + playerName + "'s §centry in database").create());
                    return;
                }

                PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
                if (permissionGroup == null) {
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while searching §b" + permissionUser.name() + "'s §centry in database").create());
                    return;
                }

                if (this.plugin.permissionManagement.highestPermissionGroup(permissionUser).potency() < this.plugin.potencyContentCreator) {
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cDieser Spieler kann keine Wildcards vergeben").create());
                    return;
                }

                if (!this.plugin.wildcardDatabase.saved(fetched)) {
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + permissionGroup.color() + " §cist nicht gewhitelisted").create());
                    return;
                }

                WildcardDatabase.WildcardData wildcardData = this.plugin.wildcardDatabase.data(fetched);
                player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fDer Spieler " + permissionGroup.color() + permissionUser.name() + " §fbesitzt aktuell §a" + wildcardData.amount + " §fWildcards").create());
                player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fLetzter Reset§8: §a" + formatter.format(wildcardData.lastReset)).create());

                List<WhitelistDatabase.WhitelistData> list = this.plugin.whitelistDatabase.dataList(fetched);
                if (!list.isEmpty()) {
                    player.sendMessage(new ComponentBuilder().create());
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fEs wurden §a" + list.size() + " §fSpieler von " + permissionGroup.color() + permissionUser.name() + " §fzur Whitelist hinzugefügt").create());

                    StringBuilder users = new StringBuilder();
                    for (WhitelistDatabase.WhitelistData whitelistData : list) {
                        if (!this.plugin.permissionManagement.containsUserAsync(whitelistData.receiver).join()) continue;

                        PermissionUser whitelistedUser = this.plugin.permissionManagement.userAsync(whitelistData.receiver).join();
                        if (whitelistedUser == null) continue;

                        PermissionGroup whitelistedUsersGroup = this.plugin.permissionManagement.highestPermissionGroup(whitelistedUser);
                        if (whitelistedUsersGroup == null) continue;

                        if (users.length() > 1) users.append("§8, ");

                        users.append(whitelistedUsersGroup.color()).append(whitelistedUser.name());
                    }

                    player.sendMessage(new ComponentBuilder(" " + users.toString()).create());
                }
            }

            case 2 -> {
                if (!player.hasPermission("wintervillage.proxy.command.wildcard.extended")) {
                    player.sendMessage(new ComponentBuilder("§cYou do not have permission to execute this command!").create());
                    return;
                }

                if (args[0].equalsIgnoreCase("modify")) {
                    int amount = 0;
                    try {
                        amount = Integer.parseInt(args[1]);
                    } catch (NumberFormatException exception) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cUngültige Anzahl").create());
                        return;
                    }

                    this.plugin.wildcardDatabase.modify(amount);
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fDu hast allen §a" + amount + " §fWildcards " + (amount > 0 ? "hinzugefügt" : "entfernt")).create());
                    return;
                }

                if (args[0].equalsIgnoreCase("reset")) {
                    String playerName = args[1];
                    UUID fetched = this.plugin.mojangAPIFetcher.uuidFrom(playerName);
                    if (fetched == null) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while fetching the uuid from §b" + playerName).create());
                        return;
                    }

                    if (!this.plugin.permissionManagement.containsUserAsync(fetched).join()) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while searching §b" + playerName + "'s §centry in database").create());
                        return;
                    }
                    PermissionUser permissionUser = this.plugin.permissionManagement.user(fetched);
                    if (permissionUser == null) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while searching §b" + playerName + "'s §centry in database").create());
                        return;
                    }

                    PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
                    if (permissionGroup == null) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while searching §b" + permissionUser.name() + "'s §centry in database").create());
                        return;
                    }

                    if (this.plugin.permissionManagement.highestPermissionGroup(permissionUser).potency() < this.plugin.potencyContentCreator) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cDieser Spieler kann keine Wildcards vergeben").create());
                        return;
                    }

                    this.plugin.wildcardDatabase.reset(fetched);
                    this.plugin.wildcardDatabase.reset(fetched, LocalDateTime.now());

                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fDu hast dem Spieler " + permissionGroup.color().replace("&", "§") + playerName + " §fseine Wildcards zurückgesetzt").create());
                    return;
                }

                error(player);
            }

            case 3 -> {
                if (!player.hasPermission("wintervillage.proxy.command.wildcard.extended")) {
                    player.sendMessage(new ComponentBuilder("§cYou do not have permission to execute this command!").create());
                    return;
                }

                String playerName = args[1];
                UUID fetched = this.plugin.mojangAPIFetcher.uuidFrom(playerName);
                if (fetched == null) {
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while fetching the uuid from §b" + playerName).create());
                    return;
                }

                int amount = 0;
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException exception) {
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cUngültige Anzahl").create());
                    return;
                }

                if (args[0].equalsIgnoreCase("modify")) {
                    if (!this.plugin.permissionManagement.containsUserAsync(fetched).join()) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while searching §b" + playerName + "'s §centry in database").create());
                        return;
                    }

                    PermissionUser permissionUser = this.plugin.permissionManagement.user(fetched);
                    if (this.plugin.permissionManagement.highestPermissionGroup(permissionUser).potency() < this.plugin.potencyContentCreator) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cDieser Spieler kann keine Wildcards vergeben").create());
                        return;
                    }

                    if (!this.plugin.wildcardDatabase.saved(permissionUser.uniqueId()))
                        this.plugin.wildcardDatabase.save(permissionUser.uniqueId(), LocalDateTime.now());

                    PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);

                    this.plugin.wildcardDatabase.modify(fetched, amount);
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fDem Spieler " + permissionGroup.color().replace("&", "§") + playerName + " §fwurden §a" + amount + " §fWildcards " + (amount > 0 ? "hinzugefügt" : "entfernt")).create());
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fEr besitzt jetzt noch§8: §e" + this.plugin.wildcardDatabase.amount(fetched)).create());
                    return;
                }

                error(player);
            }

            default -> error(player);
        }
    }

    private void error(ProxiedPlayer player) {
        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cFalsche Syntax! Verwende folgendermaßen:").create());
        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§a/wildcard").create());
        if (player.hasPermission("wintervillage.proxy.command.wildcard.extended")) {
            player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§a/wildcard <Spieler>").create());
            player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§a/wildcard reset <Spieler>").create());
            player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§a/wildcard modify (Spieler) <Anzahl>").create());
        }
    }
}