package de.voldechse.wintervillage.aura.listener;

import de.voldechse.wintervillage.aura.Aura;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final Aura plugin = InjectionLayer.ext().instance(Aura.class);

    @EventHandler
    public void execute(PlayerDeathEvent event) {
        event.setDeathMessage(null);
        event.setDroppedExp(0);
        event.getDrops().clear();

        Player diedPlayer = event.getEntity();

        diedPlayer.getScoreboard().getTeams().forEach(org.bukkit.scoreboard.Team::unregister);

        this.plugin.gameManager.addSpectator(diedPlayer);

        if (!plugin.permissionManagement.containsUserAsync(diedPlayer.getUniqueId()).join()) return;

        PermissionUser permissionUser = plugin.permissionManagement.userAsync(diedPlayer.getUniqueId()).join();
        if (permissionUser == null) return;
        PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) return;

        if (diedPlayer.getKiller() != null) {
            Player killer = diedPlayer.getKiller();

            if (!plugin.permissionManagement.containsUserAsync(killer.getUniqueId()).join()) return;

            PermissionUser killerPermissionUser = plugin.permissionManagement.userAsync(killer.getUniqueId()).join();
            if (killerPermissionUser == null) return;
            PermissionGroup killersPermissionGroup = plugin.permissionManagement.highestPermissionGroup(killerPermissionUser);
            if (killersPermissionGroup == null) return;

            killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            killer.sendMessage(this.plugin.serverPrefix + "§7Du hast den Spieler " + permissionGroup.color() + permissionUser.name() + " §7getötet");

            diedPlayer.sendMessage(this.plugin.serverPrefix + "§cDu wurdest von " + killersPermissionGroup.color() + killerPermissionUser.name() + " §cgetötet");

            Bukkit.broadcastMessage(this.plugin.serverPrefix + permissionGroup.color() + permissionUser.name() + " §7wurde von " + killersPermissionGroup.color() + killerPermissionUser.name() + " §7getötet");
        } else Bukkit.broadcastMessage(this.plugin.serverPrefix + permissionGroup.color() + permissionUser.name() + " §7ist gestorben");

        if (!this.plugin.gameManager.checkGame())
            Bukkit.broadcastMessage(this.plugin.serverPrefix + "§fEs verbleiben noch §e" + this.plugin.gameManager.getLivingPlayers().size() + " §fSpieler");
        this.plugin.scoreboardManager.updateScoreboard("currentPlayers", " §a" + this.plugin.gameManager.getLivingPlayers().size() + "§8/§a" + this.plugin.PLAYING, "");
    }
}