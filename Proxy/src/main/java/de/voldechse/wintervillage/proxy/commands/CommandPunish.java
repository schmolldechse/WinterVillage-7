package de.voldechse.wintervillage.proxy.commands;

import de.voldechse.wintervillage.proxy.Proxy;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.UUID;

public class CommandPunish extends Command {

    private final Proxy plugin;

    public CommandPunish(Proxy plugin) {
        super("punish", "wintervillage.proxy.command.punish");
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

                String playerName = args[0];
                UUID fetched = this.plugin.mojangAPIFetcher.uuidFrom(playerName);
                if (fetched == null) {
                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cAn error occurred while fetching the uuid from §b" + playerName).create());
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

                if (this.plugin.banDatabase.banned(fetched)) {
                    this.plugin.banDatabase.remove(fetched);

                    player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + whitelistedsRole.color() + whitelisted.name() + " §fwurde entbannt").create());
                    return;
                }

                this.plugin.banDatabase.ban(fetched);
                player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + whitelistedsRole.color() + whitelisted.name() + " §fwurde gebannt").create());

                if (this.plugin.getInstance().getProxy().getPlayer(fetched) != null)
                    this.plugin.getInstance().getProxy().getPlayer(fetched).disconnect(new ComponentBuilder("§cDu wurdest gebannt").append("\n\n").append("§bdiscord.wintervillage.de").create());
            }

            default -> error(player);
        }
    }

    private void error(ProxiedPlayer player) {
        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§cFalsche Syntax! Verwende folgendermaßen:").create());
        player.sendMessage(new ComponentBuilder(this.plugin.proxyPrefix + "§a/punish <Spieler>").create());
    }
}