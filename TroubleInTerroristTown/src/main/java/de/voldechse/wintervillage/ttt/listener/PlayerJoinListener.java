package de.voldechse.wintervillage.ttt.listener;

import com.mojang.authlib.properties.Property;
import de.voldechse.wintervillage.library.countdown.Countdown;
import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.game.corpse.PacketReader;
import de.voldechse.wintervillage.ttt.game.corpse.player.SkinData;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import eu.cloudnetservice.driver.permission.PermissionGroup;
import eu.cloudnetservice.driver.permission.PermissionUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @EventHandler
    public void execute(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.plugin.gameManager.clearPlayer(player, true);

        PacketReader packetReader = new PacketReader(event.getPlayer());
        packetReader.inject();

        if (player.hasMetadata("RANDOM_PLAYER_FOR_RANDOMTESTER"))
            this.plugin.removeMetadata(player, "RANDOM_PLAYER_FOR_RANDOMTESTER");
        if (player.hasMetadata("TRAITOR_DETECTOR_WARNING"))
            this.plugin.removeMetadata(player, "TRAITOR_DETECTOR_WARNING");
        if (player.hasMetadata("SAVED_RESPAWN_POSITION"))
            this.plugin.removeMetadata(player, "SAVED_RESPAWN_POSITION");
        if (player.hasMetadata("SCOREBOARD_SIDEBAR")) this.plugin.removeMetadata(player, "SCOREBOARD_SIDEBAR");
        if (player.hasMetadata("USED_RANDOMTESTER")) this.plugin.removeMetadata(player, "USED_RANDOMTESTER");
        if (player.hasMetadata("USING_PARACHUTE")) this.plugin.removeMetadata(player, "USING_PARACHUTE");
        if (player.hasMetadata("INNOCENT_TICKET")) this.plugin.removeMetadata(player, "INNOCENT_TICKET");
        if (player.hasMetadata("BUSTED_TRAITOR")) this.plugin.removeMetadata(player, "BUSTED_TRAITOR");
        if (player.hasMetadata("CREEPER_ARROW")) this.plugin.removeMetadata(player, "CREEPER_ARROW");
        if (player.hasMetadata("FLAMMENWERFER")) this.plugin.removeMetadata(player, "FLAMMENWERFER");
        if (player.hasMetadata("SHOOT_SNOWMAN")) this.plugin.removeMetadata(player, "SHOOT_SNOWMAN");
        if (player.hasMetadata("TOTEM_TASKID")) this.plugin.removeMetadata(player, "TOTEM_TASKID");
        if (player.hasMetadata("SETUP_TESTER")) this.plugin.removeMetadata(player, "SETUP_TESTER");
        if (player.hasMetadata("LAST_DAMAGER")) this.plugin.removeMetadata(player, "LAST_DAMAGER");
        if (player.hasMetadata("SHOP_POINTS")) this.plugin.removeMetadata(player, "SHOP_POINTS");
        if (player.hasMetadata("C4_COOLDOWN")) this.plugin.removeMetadata(player, "C4_COOLDOWN");
        if (player.hasMetadata("TRAP_TICKET")) this.plugin.removeMetadata(player, "TRAP_TICKET");
        if (player.hasMetadata("EDIT_SPAWNS")) this.plugin.removeMetadata(player, "EDIT_SPAWNS");
        if (player.hasMetadata("EDIT_CHESTS")) this.plugin.removeMetadata(player, "EDIT_CHESTS");
        if (player.hasMetadata("BUILD_MODE")) this.plugin.removeMetadata(player, "BUILD_MODE");
        if (player.hasMetadata("DEFROSTING")) this.plugin.removeMetadata(player, "DEFROSTING");
        if (player.hasMetadata("DEFUSING")) this.plugin.removeMetadata(player, "DEFUSING");
        if (player.hasMetadata("BLASROHR")) this.plugin.removeMetadata(player, "BLASROHR");
        if (player.hasMetadata("BUMERANG")) this.plugin.removeMetadata(player, "BUMERANG");
        if (player.hasMetadata("EDITING")) this.plugin.removeMetadata(player, "EDITING");
        if (player.hasMetadata("SNOWMAN")) this.plugin.removeMetadata(player, "SNOWMAN");

        Types gamePhase = this.plugin.gameStateManager.currentGameState().getGameStatePhase();
        if (gamePhase != Types.LOBBY) {
            event.setJoinMessage(null);

            this.plugin.gameManager.setSpectator(player, true);
            this.plugin.CORPSES_MAP.forEach((entityId, corpse) -> corpse.respawnCorpse(player, entityId));
            return;
        }

        this.plugin.getInstance().getLogger().info("Store players [" + player.getName() + "] texture data to spawn corpse if needed");
        Property property = ((CraftPlayer) player).getProfile().getProperties().get("textures").iterator().next();
        SkinData playerSkinData = new SkinData(property.getValue(), property.getSignature());
        this.plugin.playerSkinData.put(player.getUniqueId(), playerSkinData);

        /**
         * CLOUD IMPLEMENTATION
         */
        if (!plugin.permissionManagement.containsUserAsync(player.getUniqueId()).join()) return;

        PermissionUser permissionUser = plugin.permissionManagement.userAsync(player.getUniqueId()).join();
        if (permissionUser == null) return;
        PermissionGroup permissionGroup = plugin.permissionManagement.highestPermissionGroup(permissionUser);
        if (permissionGroup == null) return;

        event.setJoinMessage("§a» " + permissionGroup.color() + permissionUser.name() + " §fhat das Spiel betreten");

        this.plugin.scoreboardManager.playerList();

        player.teleport(getLobbyLocation());

        Countdown countdown = this.plugin.gameStateManager.currentGameState().getCountdown();
        boolean enoughPlayers = this.plugin.roleManager.getPlayerList().size() >= this.plugin.minPlayers;
        boolean allowedToStart = countdown.isSleeping() && enoughPlayers;

        if (!allowedToStart) return;
        if (this.plugin.PAUSED) return;

        countdown.stopCountdown(false);
        countdown.setInitializedTime(this.plugin.lobbyCountdown);
        countdown.startCountdown(this.plugin.lobbyCountdown, false);
    }

    public Location getLobbyLocation() {
        Document document = this.plugin.configDocument.getDocument("lobbySpawn");
        return new Location(
                Bukkit.getWorld(document.getString("world")),
                document.getDouble("x"),
                document.getDouble("y"),
                document.getDouble("z"),
                document.getFloat("yaw"),
                document.getFloat("pitch")
        );
    }
}