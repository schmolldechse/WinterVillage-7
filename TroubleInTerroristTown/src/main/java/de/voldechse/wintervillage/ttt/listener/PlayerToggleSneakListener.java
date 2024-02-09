package de.voldechse.wintervillage.ttt.listener;

import com.mojang.authlib.properties.Property;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.game.corpse.CorpseEntity;
import de.voldechse.wintervillage.ttt.game.corpse.Hologram;
import de.voldechse.wintervillage.ttt.game.corpse.player.CorpseData;
import de.voldechse.wintervillage.ttt.game.corpse.player.SkinData;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.roles.Role;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PlayerToggleSneakListener implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @EventHandler
    public void execute(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        if (this.plugin.gameManager.isSpectator(player)) return;

        if (getNearestEntity(player) == null) return;
        Entity nearest = getNearestEntity(player);
        if (!(nearest instanceof ArmorStand)) return;
        if (!nearest.hasMetadata("CORPSE_entityId")) return;

        if (this.plugin.gameStateManager.currentGameState().getGameStatePhase() != Types.INGAME) return;

        int entityId = nearest.getMetadata("CORPSE_entityId").get(0).asInt();
        CorpseEntity corpse = this.plugin.CORPSES_MAP.get(entityId);
        if (corpse == null) return;

        Role role = this.plugin.roleManager.getRole(player);
        if (role == null) return;
        if (role.roleId != 1) return;

        CorpseData corpseData = corpse.corpseData;

        if (corpseData.getDocument().contains("revealed") && !corpseData.getDocument().getBoolean("revealed")) {
            player.sendMessage(this.plugin.serverPrefix + "§7Die Spurensicherung arbeitet noch ...");
            return;
        }

        if (corpseData.isIdentified()) {
            player.sendMessage(this.plugin.serverPrefix + "§cDiese Leiche wurde bereits untersucht");
            return;
        }

        this.plugin.SNEAK_ARMORSTANDS_USELESS.remove(entityId);

        if (corpseData.getDocument().contains("fakeCorpse")) {
            UUID spawnedByUUID = UUID.fromString(corpseData.getDocument().getString("source"));
            Player playerWhoSpawnedCorpse = Bukkit.getPlayer(spawnedByUUID);

            Location fakeCorpseLocation = corpse.location.clone();

            Bukkit.getOnlinePlayers().forEach(players -> corpse.despawnCorpse(players, entityId));
            this.plugin.CORPSES_MAP.remove(entityId);
            nearest.remove();

            TNTPrimed tnt = fakeCorpseLocation.getWorld().spawn(fakeCorpseLocation, TNTPrimed.class);
            tnt.setGravity(true);
            tnt.setIsIncendiary(false);
            tnt.setSource(player);
            tnt.setFuseTicks(1);
            tnt.setYield(2.5F);
            if (playerWhoSpawnedCorpse != null) tnt.setSource(playerWhoSpawnedCorpse);

            this.plugin.setMetadata(tnt, "SUMMONED_BY", player.getUniqueId());
            //player.sendMessage(this.plugin.serverPrefix + "§cDu hast einen §3Boom-Body §cidentifiziert!");
            player.sendMessage(this.plugin.serverPrefix + "§cDu hast einen §3Falschen Elf §cidentifiziert!");

            if (PlayerMoveListener.nearCorpse.contains(player.getUniqueId()))
                PlayerMoveListener.nearCorpse.remove(player.getUniqueId());
            return;
        }

        Location corpseLocation = corpse.location;
        Document oldDocument = corpseData.getDocument();

        Bukkit.getOnlinePlayers().forEach(players -> corpse.despawnCorpse(players, entityId));
        this.plugin.CORPSES_MAP.remove(entityId);
        Bukkit.broadcastMessage(this.plugin.serverPrefix + "§7Die Leiche von " +
                oldDocument.getString("diedPlayer_PREFIX") +
                oldDocument.getString("diedPlayer_NAME") + " §8(" +
                oldDocument.getString("diedPlayer_ROLE") + "§8) §7wurde gefunden");

        UUID diedPlayer = UUID.fromString(oldDocument.getString("diedPlayer_UUID"));
        SkinData diedPlayerSkinData = this.plugin.playerSkinData.get(diedPlayer);

        oldDocument.append("identifier", player.getUniqueId());
        Hologram hologram = new Hologram(
                oldDocument,
                "§7Leiche von " +
                        oldDocument.getString("diedPlayer_PREFIX") +
                        oldDocument.getString("diedPlayer_NAME") + " §8(" +
                        oldDocument.getString("diedPlayer_ROLE") + "§8)",
                "§7Tot seit §e00:00",
                "§7Getötet von §4✘");
        hologram.spawn(corpseLocation.clone(), oldDocument.getLong("TIMESTAMP"));

        this.plugin.roleManager.changeShopPoints(player, 1);
        this.plugin.scoreboardManager.updateScoreboard(player, "currentShopPoints", " §9" + this.plugin.roleManager.getShopPoints(player), " §b");
        //player.sendMessage(this.plugin.serverPrefix + "§aLeiche identifiziert §b+§e1 §9Detective-Punkt");
        player.sendMessage(this.plugin.serverPrefix + "§aLeiche identifiziert §b+§e1 §9Mr. Frost-Punkt");
        //TODO: player.sendMessage(this.plugin.serverPrefix + "§7Mit dem §5Super-Identifizierer §7kannst du noch mehr Informationen aus der Leiche erhalten");

        CorpseData newCorpseData = new CorpseData(
                corpseLocation,
                new Property("textures", diedPlayerSkinData.getValue(), diedPlayerSkinData.getSignature()),
                oldDocument.append("revealed", false)
        );
        newCorpseData.setIdentified(true);
        newCorpseData.setSavedShopPoints(corpseData.getSavedShopPoints());
        CorpseEntity newCorpse = new CorpseEntity(newCorpseData);
        newCorpse.spawn(true);

        this.plugin.setMetadata(nearest, "CORPSE_entityId", newCorpse.corpse.getId());

        startIdentificationScheduler(newCorpseData);

        if (PlayerMoveListener.nearCorpse.contains(player.getUniqueId()))
            PlayerMoveListener.nearCorpse.remove(player.getUniqueId());
    }

    private Entity getNearestEntity(Player player) {
        double lowestDistance = Double.MAX_VALUE;
        Entity closestEntity = null;

        for (Entity entity : player.getNearbyEntities(1.5D, 1D, 1.5D)) {
            double distance = entity.getLocation().distanceSquared(player.getLocation());
            if (distance < lowestDistance) {
                lowestDistance = distance;
                closestEntity = entity;
            }
        }

        if (closestEntity != null) return closestEntity;
        return null;
    }

    private void startIdentificationScheduler(CorpseData corpseData) {
        Document document = corpseData.getDocument();

        UUID diedPlayer_uuid = UUID.fromString(document.getString("diedPlayer_UUID"));
        UUID killer_uuid = null;
        if (document.contains("killer_UUID")) killer_uuid = UUID.fromString(document.getString("killer_UUID"));

        ArmorStand TIMER = this.plugin.IDENTIFICATION_MAP_TIMER.get(diedPlayer_uuid);
        ArmorStand REVEAL_KILLER = this.plugin.IDENTIFICATION_MAP_KILLED_BY.get(killer_uuid == null ? diedPlayer_uuid : killer_uuid);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.gameStateManager.currentGameState().getGameStatePhase() != Types.INGAME) {
                    cancel();
                    return;
                }

                if (plugin.CORPSES_MAP.get(corpseData.getEntityId()) == null) {
                    TIMER.remove();
                    cancel();
                    return;
                }

                long startTime = TIMER.getMetadata("IDENTIFICATION_TIMER").get(0).asLong();
                long currentTime = System.currentTimeMillis();

                long differenceInSeconds = (currentTime - startTime) / 1000;

                TIMER.setCustomName("§7Tot seit: §e" + String.format("%02d:%02d", (differenceInSeconds / 60), (differenceInSeconds % 60)));
            }
        }.runTaskTimer(this.plugin.getInstance(), 0L, 20L);

        String customName = "";
        if (killer_uuid == null) customName = "§cUNBEKANNT";
        else customName = document.getString("killer_PREFIX") + document.getString("killer_NAME");

        String finalCustomName = customName;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.gameStateManager.currentGameState().getGameStatePhase() != Types.INGAME) {
                    cancel();
                    return;
                }

                if (plugin.CORPSES_MAP.get(corpseData.getEntityId()) == null) {
                    REVEAL_KILLER.remove();
                    cancel();
                    return;
                }

                REVEAL_KILLER.setCustomName("§7Getötet von §f" + finalCustomName);

                Player identifier = Bukkit.getPlayer(UUID.fromString(document.getString("identifier")));
                if (identifier != null) {
                    identifier.sendMessage(plugin.serverPrefix + "§7Die Detektei hat die Ermittlungen über den Tod von " + document.getString("diedPlayer_PREFIX") + document.getString("diedPlayer_NAME") + " §7beendet. Kehre zur Leiche zurück um die Informationen zu erfahren");
                    if (corpseData.getSavedShopPoints() > 0) {
                        //identifier.sendMessage(this.plugin.serverPrefix + "§aIn der Leiche waren noch Punkte gespeichert §b+§e" + corpseData.getSavedShopPoints() + " §9Detective-Punkt" + (corpseData.getSavedShopPoints() > 1 ? "e" : ""));
                        identifier.sendMessage(plugin.serverPrefix + "§aIn der Leiche waren noch Punkte gespeichert §b+§e" + corpseData.getSavedShopPoints() + " §9Mr. Frost-Punkt" + (corpseData.getSavedShopPoints() > 1 ? "§e" : ""));

                        plugin.roleManager.changeShopPoints(identifier, corpseData.getSavedShopPoints());
                        plugin.scoreboardManager.updateScoreboard(identifier, "currentShopPoints", " §9" + plugin.roleManager.getShopPoints(identifier), " §b");
                    }
                }

                corpseData.getDocument().remove("revealed");
                corpseData.getDocument().append("revealed", true);
            }
        }.runTaskLater(this.plugin.getInstance(), 30 * 20L);
    }
}