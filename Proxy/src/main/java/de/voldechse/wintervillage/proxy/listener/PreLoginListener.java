package de.voldechse.wintervillage.proxy.listener;

import de.voldechse.wintervillage.proxy.Proxy;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.time.LocalDateTime;
import java.util.UUID;

public class PreLoginListener implements Listener {

    private final Proxy proxy = InjectionLayer.ext().instance(Proxy.class);

    @EventHandler
    public void execute(PreLoginEvent event) {
        UUID fetched = this.proxy.mojangAPIFetcher.uuidFrom(event.getConnection().getName());
        if (fetched == null) {
            event.setCancelled(true);
            event.setCancelReason(new ComponentBuilder("§cEin Fehler ist während des Verarbeiten deiner UUID aufgetreten").append("\n").append("§cVersuche es erneut").append("\n\n").append("§bdiscord.wintervillage.de").create());
            return;
        }

        PermissionUser permissionUser = this.proxy.permissionManagement.getOrCreateUserAsync(fetched, event.getConnection().getName()).join();
        this.proxy.permissionManagement.updateUserAsync(permissionUser).join();
        PermissionGroup permissionGroup = this.proxy.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) {
            event.setCancelled(true);
            event.setCancelReason(new ComponentBuilder("§cDu benötigst eine Wildcard, um mitzuspielen").append("\n\n").append("§bdiscord.wintervillage.de").create());
            return;
        }

        if (this.proxy.banDatabase.banned(fetched)) {
            event.setCancelled(true);
            event.setCancelReason(new ComponentBuilder("§cDu bist von WinterVillage gebannt").append("\n\n").append("§bdiscord.wintervillage.de").create());
            return;
        }

        if (!this.proxy.whitelistDatabase.whitelisted(fetched) && permissionGroup.potency() >= this.proxy.potencySupporter)
            this.proxy.whitelistDatabase.whitelist(fetched, new UUID(0, 0), LocalDateTime.now());

        if (permissionGroup.potency() >= this.proxy.potencyContentCreator && !this.proxy.wildcardDatabase.saved(permissionUser.uniqueId()))
            this.proxy.wildcardDatabase.save(permissionUser.uniqueId(), LocalDateTime.now());

        if (this.proxy.WHITELIST_ENABLED) {
            if (this.proxy.WHITELIST_PRIORITY && permissionGroup.potency() < this.proxy.potencyContentCreator) {
                event.setCancelled(true);
                event.setCancelReason(new ComponentBuilder("§cEs können aktuell nur Spieler mit §bContentCreator §coder höher den Server betreten").append("\n\n").append("§bdiscord.wintervillage.de").create());
                return;
            }

            if (!(permissionGroup.hasPermission("wintervillage.whitelisted").asBoolean() || this.proxy.whitelistDatabase.whitelisted(fetched))) {
                event.setCancelled(true);
                event.setCancelReason(new ComponentBuilder("§cDu benötigst eine Wildcard, um mitzuspielen").append("\n\n").append("§bdiscord.wintervillage.de").create());
                return;
            }
        }

        this.proxy.getInstance().getLogger().info("Fetched logging in [uuid=" + fetched + "]");
    }
}