package de.voldechse.wintervillage.ttt.game.corpse;

import com.mojang.authlib.GameProfile;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.game.corpse.player.CorpseData;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.scoreboard.CraftScoreboard;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CorpseEntity {

    private final TTT plugin;
    
    public Location location;

    public CorpseData corpseData;

    public ServerPlayer corpse;

    private GameProfile gameProfile;

    public CorpseEntity(CorpseData corpseData) {
        this.plugin = InjectionLayer.ext().instance(TTT.class);
        
        this.location = corpseData.getLocation();
        this.corpseData = corpseData;

        this.gameProfile = new GameProfile(UUID.randomUUID(), "");
        this.gameProfile.getProperties().put("textures", corpseData.getProperty());

        this.corpse = new ServerPlayer(
                ((CraftServer) Bukkit.getServer()).getServer(),
                ((CraftWorld) location.getWorld()).getHandle(),
                this.gameProfile
        );
        this.corpse.setPos(location.getX(), location.getY(), location.getZ());
        this.corpse.setXRot(location.getYaw());
        this.corpse.setYRot(location.getPitch());

        this.corpseData.setEntityId(this.corpse.getId());
    }

    public void spawn(boolean spawnArmorStand) {
        byte b = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40;

        PlayerTeam playerTeam = new PlayerTeam(((CraftScoreboard) Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(), "");
        playerTeam.setNameTagVisibility(Team.Visibility.NEVER);
        playerTeam.getPlayers().addAll(Arrays.asList(""));

        ClientboundSetPlayerTeamPacket score1 = ClientboundSetPlayerTeamPacket.createRemovePacket(playerTeam);
        ClientboundSetPlayerTeamPacket score2 = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerTeam, true);
        ClientboundSetPlayerTeamPacket score3 = ClientboundSetPlayerTeamPacket.createPlayerPacket(playerTeam, String.valueOf(corpse.getName()), ClientboundSetPlayerTeamPacket.Action.ADD);

        corpse.setPose(Pose.SWIMMING);

        ClientboundMoveEntityPacket moveEntityPacket = new ClientboundMoveEntityPacket.Pos(
                corpse.getId(), (byte) 0, (byte) ((location.getY() - 1.7 - location.getY()) * 32),
                (byte) 0, false);

        SynchedEntityData dataWatcher = corpse.getEntityData();
        dataWatcher.set(EntityDataSerializers.BYTE.createAccessor(17), b);
        corpseData.setCorpse(corpse);

        Bukkit.getOnlinePlayers().forEach(player -> {
            ServerGamePacketListenerImpl playerConnection = ((CraftPlayer) player).getHandle().connection;
            playerConnection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, corpse));
            playerConnection.send(new ClientboundAddPlayerPacket(corpse));

            playerConnection.send(score1);
            playerConnection.send(score2);
            playerConnection.send(score3);

            playerConnection.send(new ClientboundSetEntityDataPacket(corpse.getId(), dataWatcher.getNonDefaultValues()));
            playerConnection.send(moveEntityPacket);

            new BukkitRunnable() {
                @Override
                public void run() {
                    playerConnection.send(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(corpse.getUUID())));
                }
            }.runTaskAsynchronously(this.plugin.getInstance());
        });

        if (spawnArmorStand) {
            ArmorStand armorStand = location.getWorld().spawn(location, ArmorStand.class);
            armorStand.setVisible(false);
            armorStand.setInvulnerable(true);
            armorStand.setGravity(false);
            this.plugin.setMetadata(armorStand, "CORPSE_entityId", corpse.getId());
            this.plugin.SNEAK_ARMORSTANDS_USELESS.put(corpse.getId(), armorStand);
        }

        this.plugin.CORPSES_MAP.put(corpse.getId(), this);
    }

    public void respawnCorpse(Player player, int entityId) {
        if (!this.plugin.CORPSES_MAP.containsKey(entityId)) return;

        byte b = 0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40;

        PlayerTeam playerTeam = new PlayerTeam(((CraftScoreboard) Bukkit.getScoreboardManager().getMainScoreboard()).getHandle(), "");
        playerTeam.setNameTagVisibility(Team.Visibility.NEVER);
        playerTeam.getPlayers().addAll(Arrays.asList(""));

        ClientboundSetPlayerTeamPacket score1 = ClientboundSetPlayerTeamPacket.createRemovePacket(playerTeam);
        ClientboundSetPlayerTeamPacket score2 = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerTeam, true);
        ClientboundSetPlayerTeamPacket score3 = ClientboundSetPlayerTeamPacket.createPlayerPacket(playerTeam, String.valueOf(corpse.getName()), ClientboundSetPlayerTeamPacket.Action.ADD);

        corpse.setPose(Pose.SWIMMING);
        if (corpse.getId() != entityId) corpse.setId(entityId);

        ClientboundMoveEntityPacket moveEntityPacket = new ClientboundMoveEntityPacket.Pos(
                corpse.getId(), (byte) 0, (byte) ((location.getY() - 1.7 - location.getY()) * 32),
                (byte) 0, false);

        SynchedEntityData dataWatcher = corpse.getEntityData();
        dataWatcher.set(EntityDataSerializers.BYTE.createAccessor(17), b);
        corpseData.setCorpse(corpse);

        ServerGamePacketListenerImpl playerConnection = ((CraftPlayer) player).getHandle().connection;
        playerConnection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, corpse));
        playerConnection.send(new ClientboundAddPlayerPacket(corpse));

        playerConnection.send(score1);
        playerConnection.send(score2);
        playerConnection.send(score3);

        playerConnection.send(new ClientboundSetEntityDataPacket(corpse.getId(), dataWatcher.getNonDefaultValues()));
        playerConnection.send(moveEntityPacket);

        new BukkitRunnable() {
            @Override
            public void run() {
                playerConnection.send(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(corpse.getUUID())));
            }
        }.runTaskAsynchronously(this.plugin.getInstance());
    }

    public void despawnCorpse(Player player, int entityId) {
        if (!this.plugin.CORPSES_MAP.containsKey(entityId)) return;

        ServerGamePacketListenerImpl playerConnection = ((CraftPlayer) player).getHandle().connection;
        playerConnection.send(new ClientboundRemoveEntitiesPacket(entityId));
        playerConnection.send(new ClientboundPlayerInfoRemovePacket(List.of(this.plugin.getCorpse(entityId).corpse.getUUID())));
    }
}