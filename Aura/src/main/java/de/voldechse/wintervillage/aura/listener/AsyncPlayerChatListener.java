package de.voldechse.wintervillage.aura.listener;

import de.voldechse.wintervillage.aura.Aura;
import de.voldechse.wintervillage.aura.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncPlayerChatListener implements Listener {

    private final Aura plugin = InjectionLayer.ext().instance(Aura.class);

    @EventHandler
    public void execute(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        PermissionUser permissionUser = this.plugin.permissionManagement.getOrCreateUserAsync(player.getUniqueId(), player.getName()).join();
        if (permissionUser == null) {
            event.setCancelled(true);
            player.sendMessage(plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
            return;
        }
        PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) {
            event.setCancelled(true);
            player.sendMessage(plugin.serverPrefix + "§cAn error occurred while searching your entry in database");
            return;
        }

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        switch (gamePhase) {
            case LOBBY -> {
                event.getRecipients().clear();
                event.getRecipients().addAll(Bukkit.getOnlinePlayers());

                event.setFormat(" " + permissionGroup.color() + permissionGroup.name() + " §f%1$s" + " §8| §f%2$s");
            }

            case PREPARING_START, INGAME -> {
                if (this.plugin.gameManager.isSpectator(player)) {
                    event.getRecipients().clear();
                    event.getRecipients().addAll(this.plugin.gameManager.getSpectatorList());

                    event.setFormat(" §4✘ " + permissionGroup.color() + permissionGroup.name() + " §f%1$s §8| §f%2$s");
                    return;
                }

                event.getRecipients().clear();
                event.getRecipients().addAll(Bukkit.getOnlinePlayers());

                event.setFormat(" " + permissionGroup.color() + permissionGroup.name() + " §f%1$s §8| §f%2$s");
            }

            case RESTART -> {
                event.getRecipients().clear();
                event.getRecipients().addAll(Bukkit.getOnlinePlayers());

                event.setFormat(" " + permissionGroup.color() + permissionGroup.name() + " §f%1$s §8| §f%2$s");
            }
        }
    }
}