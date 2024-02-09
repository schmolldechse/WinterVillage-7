package de.voldechse.wintervillage.ttt.listener;

import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.game.corpse.CorpseEntity;
import de.voldechse.wintervillage.ttt.game.corpse.player.CorpseData;
import de.voldechse.wintervillage.ttt.roles.Role;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scoreboard.Team;

import java.util.Objects;
import java.util.UUID;

public class PlayerDeathListener implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    public static boolean LAST_TRAITOR = false;

    @EventHandler
    public void execute(PlayerDeathEvent event) {
        event.setDeathMessage(null);
        event.setDroppedExp(0);
        event.getDrops().clear();

        Player diedPlayer = event.getEntity();

        diedPlayer.getScoreboard().getTeams().stream()
                .filter(team -> team.hasEntry(diedPlayer.getName()))
                .forEach(Team::unregister);

        Role role = this.plugin.roleManager.getRole(diedPlayer);
        switch (role.roleId) {
            case 0 -> this.plugin.scoreboardManager.removeEntry(diedPlayer, "2_INNOCENT");
            case 1 -> this.plugin.scoreboardManager.removeEntry(diedPlayer, "1_DETECTIVE");
            case 2 -> this.plugin.scoreboardManager.removeEntry(diedPlayer, "3_TRAITOR");
        }

        if (diedPlayer.getKiller() != null) {
            Player killer = diedPlayer.getKiller();
            Role killerRole = this.plugin.roleManager.getRole(killer);

            killer.playSound(killer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

            int points = 0;

            if (killerRole.roleId == 0) {
                if (role.roleId == 2) points = 3;

                killer.sendMessage(this.plugin.serverPrefix + "§aDu hast " + role.getRolePrefix() + diedPlayer.getName() + " §agetötet " + (points > 0 ? "§b+§e" + points + " §aElfen-Punkte" : ""));
            } else if (killerRole.roleId == 1) {
                if (role.roleId == 2) points = 2;

                //killer.sendMessage(this.plugin.serverPrefix + "§aDu hast " + role.getRolePrefix() + diedPlayer.getName() + " §agetötet " + (points > 0 ? "§b+§e" + points + " §9Detective-Punkte" : ""));
                //killer.sendMessage(this.plugin.serverPrefix + "§aDu hast " + role.getRolePrefix() + diedPlayer.getName() + " §agetötet §b+§e" + points + " §9Mr. Frost-Punkte");
                killer.sendMessage(this.plugin.serverPrefix + "§aDu hast " + role.getRolePrefix() + diedPlayer.getName() + " §agetötet " + (points > 0 ? "§b+§e" + points + " " + killerRole.getRolePrefix() + killerRole.getRoleName() + "-Punkte" : ""));
            } else if (killerRole.roleId == 2) {
                points = 3;

                //killer.sendMessage(this.plugin.serverPrefix + "§aDu hast " + role.getRolePrefix() + diedPlayer.getName() + " §agetötet §b+§e" + points + " §4Traitor-Punkt" + (points > 1 ? "e" : ""));
                //killer.sendMessage(this.plugin.serverPrefix + "§aDu hast " + role.getRolePrefix() + diedPlayer.getName() + " §agetötet §b+§e" + (points) + " §4Krampus-Punkt" + (points > 1 ? "e" : ""));
                killer.sendMessage(this.plugin.serverPrefix + "§aDu hast " + role.getRolePrefix() + diedPlayer.getName() + " §agetötet " + (points > 1 ? "§b+§e" + points + " " + killerRole.getRolePrefix() + killerRole.getRoleName() + "-Punkte" : "-Punkt"));
            }

            this.plugin.roleManager.changeShopPoints(killer, points);
            this.plugin.scoreboardManager.updateScoreboard(killer, "currentShopPoints", " " + killerRole.getRolePrefix() + this.plugin.roleManager.getShopPoints(diedPlayer), " §b");

            killer.sendMessage(this.plugin.serverPrefix + "§cDu hast einen " + role.getRolePrefix() + role.getRoleName() + " §cgetötet");
            diedPlayer.sendMessage(this.plugin.serverPrefix + "§cDu wurdest von " + killerRole.getRolePrefix() + killer.getName() + " §cgetötet");

            CorpseData corpseData = new CorpseData(
                    diedPlayer.getLocation(),
                    this.plugin.deadSkinTexture,
                    new Document("diedPlayer_NAME", diedPlayer.getName())
                            .append("diedPlayer_UUID", diedPlayer.getUniqueId())
                            .append("diedPlayer_PREFIX", role.getRolePrefix())
                            .append("diedPlayer_ROLE", role.getRolePrefix() + role.getRoleName())
                            .append("killer_NAME", killer.getName())
                            .append("killer_UUID", killer.getUniqueId())
                            .append("killer_PREFIX", killerRole.getRolePrefix())
                            .append("killer_ROLE", killerRole.getRolePrefix() + killerRole.getRoleName())
                            .append("TIMESTAMP", System.currentTimeMillis())
            );
            corpseData.setSavedShopPoints(this.plugin.roleManager.getShopPoints(diedPlayer));
            corpseData.setIdentified(false);
            new CorpseEntity(corpseData).spawn(true);
        } else {
            Document corpseDocument = new Document("diedPlayer_NAME", diedPlayer.getName())
                    .append("diedPlayer_UUID", diedPlayer.getUniqueId())
                    .append("diedPlayer_PREFIX", role.getRolePrefix())
                    .append("diedPlayer_ROLE", role.getRolePrefix() + role.getRoleName())
                    .append("TIMESTAMP", System.currentTimeMillis());

            if (diedPlayer.hasMetadata("LAST_DAMAGER")) {
                UUID summonedFromUUID = Objects.requireNonNull((UUID) diedPlayer.getMetadata("LAST_DAMAGER").get(0).value());
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(summonedFromUUID);

                corpseDocument.append("killer_NAME", offlinePlayer.getName())
                        .append("killer_UUID", offlinePlayer.getUniqueId())
                        .append("killer_PREFIX", "§4")
                        .append("killer_ROLE", "Traitor");

                this.plugin.removeMetadata(diedPlayer, "LAST_DAMAGER");

                if (this.plugin.roleManager.isPlayerAssigned(Bukkit.getPlayer(summonedFromUUID))) {
                    Player summonedFrom = Bukkit.getPlayer(summonedFromUUID);
                    Role killerRole = this.plugin.roleManager.getRole(summonedFrom);

                    int points = role.roleId == 0 ? 1 : 3;

                    //summonedFrom.sendMessage(this.plugin.serverPrefix + "§aDu hast " + role.getRolePrefix() + diedPlayer.getName() + " §agetötet §b+§e" + points + " §4Traitor-Punkt" + (points > 1 ? "e" : ""));
                    summonedFrom.sendMessage(this.plugin.serverPrefix + "§aDu hast " + role.getRolePrefix() + diedPlayer.getName() + " §agetötet §b+§e" + points + " §4Krampus-Punkt" + (points > 1 ? "e" : ""));
                    this.plugin.roleManager.changeShopPoints(summonedFrom, points);
                    this.plugin.scoreboardManager.updateScoreboard(summonedFrom, "currentShopPoints", " §4" + this.plugin.roleManager.getShopPoints(summonedFrom), " §b");

                    summonedFrom.sendMessage(this.plugin.serverPrefix + "§cDu hast einen " + role.getRolePrefix() + role.getRoleName() + " §cgetötet");
                    diedPlayer.sendMessage(this.plugin.serverPrefix + "§cDu wurdest von " + killerRole.getRolePrefix() + summonedFrom.getName() + " §cgetötet");

                    summonedFrom.playSound(summonedFrom.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                }
            }

            CorpseData corpseData = new CorpseData(
                    diedPlayer.getLocation(),
                    this.plugin.deadSkinTexture,
                    corpseDocument
            );
            corpseData.setSavedShopPoints(this.plugin.roleManager.getShopPoints(diedPlayer));
            corpseData.setIdentified(false);
            new CorpseEntity(corpseData).spawn(true);
        }

        this.plugin.roleManager.removeFromRole(diedPlayer, role.roleId);

        if (this.plugin.roleManager.getRole(2).getPlayers().size() == 1 && !LAST_TRAITOR) {
            Player lastTraitor = this.plugin.roleManager.getRole(2).getPlayers().get(0);

            //lastTraitor.sendMessage(this.plugin.serverPrefix + "§cDu bist der letzte überlebende Traitor §b+§e1 §4Traitor-Punkt");
            lastTraitor.sendMessage(this.plugin.serverPrefix + "§cDu bist der letzte überlebende Krampus §b+§e1 §4Krampus-Punkt");
            this.plugin.roleManager.changeShopPoints(lastTraitor, 1);
            this.plugin.scoreboardManager.updateScoreboard(lastTraitor, "currentShopPoints", " §4" + this.plugin.roleManager.getShopPoints(lastTraitor), " §b");

            LAST_TRAITOR = true;
        }

        this.plugin.gameManager.checkGame();

        if (diedPlayer.hasMetadata("RANDOM_PLAYER_FOR_RANDOMTESTER"))
            this.plugin.removeMetadata(diedPlayer, "RANDOM_PLAYER_FOR_RANDOMTESTER");
        if (diedPlayer.hasMetadata("BUSTED_TRAITOR")) this.plugin.removeMetadata(diedPlayer, "BUSTED_TRAITOR");
        if (diedPlayer.hasMetadata("FLAMMENWERFER")) this.plugin.removeMetadata(diedPlayer, "FLAMMENWERFER");
        if (diedPlayer.hasMetadata("LAST_DAMAGER")) this.plugin.removeMetadata(diedPlayer, "LAST_DAMAGER");
        if (diedPlayer.hasMetadata("BLASROHR")) this.plugin.removeMetadata(diedPlayer, "BLASROHR");
        if (diedPlayer.hasMetadata("SNOWMAN")) this.plugin.removeMetadata(diedPlayer, "SNOWMAN");
    }
}