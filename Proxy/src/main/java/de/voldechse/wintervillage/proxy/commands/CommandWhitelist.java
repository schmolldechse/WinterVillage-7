package de.voldechse.wintervillage.proxy.commands;

import de.voldechse.wintervillage.proxy.Proxy;
import de.voldechse.wintervillage.proxy.wildcard.WhitelistDatabase;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public class CommandWhitelist extends Command {

    private final Proxy plugin;

    public CommandWhitelist(Proxy plugin) {
        super("whitelist", "wintervillage.proxy.command.whitelist");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        if (!(commandSender instanceof ProxiedPlayer player)) {
            commandSender.sendMessage(new ComponentBuilder("§cPlease execute this command as a player").create());
            return;
        }

        switch (args.length) {
            case 1 -> {
                if (!player.hasPermission("wintervillage.proxy.command.whitelist.extended")) {
                    player.sendMessage(new ComponentBuilder("§cYou do not have permission to execute this command").create());
                    return;
                }

                switch (args[0].toUpperCase()) {
                    case "LIST" -> {
                        player.sendMessage(new ComponentBuilder("§8---------- §cWhitelist §8----------").create());
                        player.sendMessage(new ComponentBuilder().create());

                        StringBuilder users = new StringBuilder();
                        List<UUID> uuids = this.plugin.whitelistDatabase.list();
                        for (UUID uuid : uuids) {
                            if (!this.plugin.permissionManagement.containsUserAsync(uuid).join()) continue;

                            PermissionUser permissionUser = this.plugin.permissionManagement.userAsync(uuid).join();
                            if (permissionUser == null) continue;

                            PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
                            if (users.length() > 1) users.append("§8, ");

                            users.append(permissionGroup.color()).append(permissionUser.name());
                        }

                        player.sendMessage(new ComponentBuilder(users.toString()).create());

                        player.sendMessage(new ComponentBuilder().create());
                        player.sendMessage(new ComponentBuilder("§fTotal§8: §b" + uuids.size() + " §fSpieler").create());
                        player.sendMessage(new ComponentBuilder("§8----------------------").create());

                        return;
                    }

                    case "TOGGLE" -> {
                        this.plugin.WHITELIST_ENABLED = !this.plugin.WHITELIST_ENABLED;

                        String message = this.plugin.WHITELIST_ENABLED ? "§aaktiviert" : "§cdeaktiviert";

                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fDie Whitelist ist nun " + message).create());
                        return;
                    }

                    default -> {
                        String playerName = args[0];
                        UUID fetched = this.plugin.mojangAPIFetcher.uuidFrom(playerName);
                        if (fetched == null) {
                            player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while fetching the uuid from §b" + playerName).create());
                            return;
                        }

                        if (!this.plugin.whitelistDatabase.whitelisted(fetched)) {
                            player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cDieser Spieler ist nicht auf der Whitelist").create());
                            return;
                        }

                        PermissionUser whitelisted = this.plugin.permissionManagement.getOrCreateUserAsync(fetched, playerName).join();
                        if (whitelisted == null) {
                            player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while searching §b" + playerName + "§c's entry in database").create());
                            return;
                        }

                        PermissionGroup whitelistedsRole = this.plugin.permissionManagement.highestPermissionGroup(whitelisted);
                        if (whitelistedsRole == null) {
                            player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while searching §b" + whitelisted.name() + "§c's entry in database").create());
                            return;
                        }

                        WhitelistDatabase.WhitelistData whitelistData = this.plugin.whitelistDatabase.data(fetched);
                        if (whitelistData == null) {
                            player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while searching §b" + whitelistedsRole.color().replace("&", "§") + whitelisted.name() + "'s §centry in whitelist's database").create());
                            return;
                        }

                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm");

                        if (whitelistData.from.equals(new UUID(0, 0))) {
                            player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fDaten zu§8: " + whitelistedsRole.color() + whitelisted.name()).create());
                            player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fHinzugefügt von§8: §4KONSOLE").create());
                            player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fHinzugefügt am§8: §a" + formatter.format(whitelistData.date)).create());
                            return;
                        }

                        if (!this.plugin.permissionManagement.containsUserAsync(whitelistData.from).join()) {
                            player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while searching whitelisters entry in whitelist database").create());
                            return;
                        }

                        PermissionUser whitelister = this.plugin.permissionManagement.userAsync(whitelistData.from).join();
                        PermissionGroup whitelistersGroup = this.plugin.permissionManagement.highestPermissionGroup(whitelister);
                        if (whitelistersGroup == null) {
                            player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while searching §b" + whitelister.name() + "'s §centry in database").create());
                            return;
                        }

                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fDaten zu§8: " + whitelistedsRole.color() + whitelisted.name()).create());
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fHinzugefügt von§8: " + whitelistersGroup.color() + whitelister.name()).create());
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fHinzugefügt am§8: §a" + formatter.format(whitelistData.date)).create());
                        return;
                    }
                }
            }

            case 2 -> {
                if (args[0].equalsIgnoreCase("add")) {
                    String playerName = args[1];
                    UUID fetched = this.plugin.mojangAPIFetcher.uuidFrom(playerName);
                    if (fetched == null) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while fetching the uuid from §b" + playerName).create());
                        return;
                    }

                    if (playerName.equals(player.getName())) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cDu kannst dich nicht selbst zur Whitelist hinzufügen").create());
                        return;
                    }

                    PermissionUser permissionUser = this.plugin.permissionManagement.getOrCreateUserAsync(player.getUniqueId(), player.getName()).join();
                    PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
                    if (permissionGroup == null) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while searching your entry in database").create());
                        return;
                    }

                    int amount = this.plugin.wildcardDatabase.amount(player.getUniqueId());
                    if (amount < 1 && permissionGroup.potency() < this.plugin.potencyDeveloper) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cDu hast keine Wildcards mehr").create());
                        return;
                    }

                    if (!this.plugin.permissionManagement.containsUserAsync(fetched).join()) {
                        this.plugin.permissionManagement.getOrCreateUserAsync(fetched, playerName).join();
                        this.plugin.permissionManagement.updateUserAsync(this.plugin.permissionManagement.userAsync(fetched).join()).join();
                    }

                    PermissionUser toAdd = this.plugin.permissionManagement.userAsync(fetched).join();
                    PermissionGroup toAddsGroup = this.plugin.permissionManagement.highestPermissionGroup(toAdd);
                    if (permissionGroup.potency() < this.plugin.potencyDeveloper && toAddsGroup.potency() >= this.plugin.potencyContentCreator) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cDu kannst diesen Spieler nicht zur Whitelist hinzufügen").create());
                        return;
                    }

                    if (toAddsGroup.hasPermission("wintervillage.whitelisted").asBoolean() || this.plugin.whitelistDatabase.whitelisted(fetched)) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cDieser Spieler ist bereits auf der Whitelist").create());
                        return;
                    }


                    this.plugin.whitelistDatabase.whitelist(fetched, player.getUniqueId(), LocalDateTime.now());
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fDer Spieler " + toAddsGroup.color() + toAdd.name() + " §fwurde zur Whitelist hinzugefügt").create());

                    if (permissionGroup.potency() < this.plugin.potencyDeveloper) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fDu hast noch §e" + (amount - 1) + " §fWildcards übrig").create());
                        this.plugin.wildcardDatabase.modify(player.getUniqueId(), -1);
                    }
                    return;
                }

                if (args[0].equalsIgnoreCase("remove")
                        && player.hasPermission("wintervillage.proxy.command.whitelist.extended")) {
                    String playerName = args[1];
                    UUID fetched = this.plugin.mojangAPIFetcher.uuidFrom(playerName);
                    if (fetched == null) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while fetching the uuid from §b" + playerName).create());
                        return;
                    }

                    if (playerName.equals(player.getName())) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cDu kannst dich nicht selbst von der Whitelist entfernen").create());
                        return;
                    }

                    if (!this.plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join()) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while searching your entry in database").create());
                        return;
                    }

                    PermissionUser permissionUser = this.plugin.permissionManagement.userAsync(player.getUniqueId()).join();
                    PermissionGroup permissionGroup = this.plugin.permissionManagement.highestPermissionGroup(permissionUser);
                    if (permissionGroup == null) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while searching your entry in database").create());
                        return;
                    }

                    if (!this.plugin.permissionManagement.containsUserAsync(fetched).join()) {
                        this.plugin.permissionManagement.getOrCreateUserAsync(fetched, playerName).join();
                        this.plugin.permissionManagement.updateUserAsync(this.plugin.permissionManagement.userAsync(fetched).join()).join();
                    }

                    PermissionUser toAdd = this.plugin.permissionManagement.userAsync(fetched).join();
                    PermissionGroup toAddsGroup = this.plugin.permissionManagement.highestPermissionGroup(toAdd);
                    if (permissionGroup.potency() < this.plugin.potencyDeveloper && toAddsGroup.potency() >= this.plugin.potencyContentCreator) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cDu kannst diesen Spieler nicht von der Whitelist entfernen").create());
                        return;
                    }

                    if (!this.plugin.whitelistDatabase.whitelisted(fetched)) {
                        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cDieser Spieler ist nicht auf der Whitelist").create());
                        return;
                    }

                    this.plugin.whitelistDatabase.remove(fetched);

                    if (this.plugin.getInstance().getProxy().getPlayer(fetched) != null)
                        this.plugin.getInstance().getProxy().getPlayer(fetched).disconnect(new TextComponent("§cDu wurdest von der Whitelist entfernt"));

                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fDer Spieler " + toAddsGroup.color() + toAdd.name() + " §fwurde von der Whitelist entfernt").create());
                    return;
                }

                if (args[0].equalsIgnoreCase("toggle") && args[1].equalsIgnoreCase("priority")) {
                    this.plugin.WHITELIST_PRIORITY = !this.plugin.WHITELIST_PRIORITY;

                    String message = this.plugin.WHITELIST_PRIORITY ? "§aaktiviert" : "§cdeaktiviert";

                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fDie Priorität ist nun " + message).create());
                    if (this.plugin.WHITELIST_PRIORITY) player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§fNur Spieler mit §dContentCreator §foder höher können den Server betreten, wenn die Whitelist aktiv ist").create());
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
        if (player.hasPermission("wintervillage.proxy.command.whitelist.extended")) {
            player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§a/whitelist list").create());
            player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§a/whitelist <Spieler>").create());
            player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§a/whitelist toggle (priority)").create());
            player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§a/whitelist remove <Spieler>").create());
        }
        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§a/whitelist add <Spieler>").create());
    }
}