package de.voldechse.wintervillage.ttt.game.corpse;

import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.game.corpse.events.RightClickCorpseEvent;
import de.voldechse.wintervillage.ttt.game.corpse.player.CorpseData;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.List;

public class PacketReader {
    
    private final TTT plugin;

    private final Player player;
    private int count = 0;

    public PacketReader(Player player) {
        this.plugin = InjectionLayer.ext().instance(TTT.class);
        
        this.player = player;
    }

    public boolean inject() {
        CraftPlayer nmsPlayer = (CraftPlayer) player;

        Channel channel = getChannel(nmsPlayer.getHandle().connection);
        if (channel.pipeline().get("PacketInjector") != null) return false;
        channel.pipeline().addAfter("decoder", "PacketInjector", new MessageToMessageDecoder<ServerboundInteractPacket>() {
            @Override
            protected void decode(ChannelHandlerContext channelHandlerContext, ServerboundInteractPacket packet,
                                  List<Object> list) throws Exception {
                list.add(packet);
                read(packet);
            }
        });

        return true;
    }

    public void uninject() {
        CraftPlayer nmsPlayer = (CraftPlayer) player;
        Channel channel = getChannel(nmsPlayer.getHandle().connection);
        if (channel.pipeline().get("PacketInjector") != null) channel.pipeline().remove("PacketInjector");
    }

    private void read(ServerboundInteractPacket packet) {
        count++;
        if (count == 4) {
            count = 0;
            int entityId = (int) getValue(packet, "a");
            if (!this.plugin.CORPSES_MAP.containsKey(entityId)) return;

            CorpseData corpseData = this.plugin.CORPSES_MAP.get(entityId).corpseData;

            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.getPluginManager().callEvent(new RightClickCorpseEvent(player, corpseData));
                }
            }.runTask(this.plugin.getInstance());
        }
    }

    private Object getValue(Object instance, String name) {
        Object result = null;
        try {
            Field field = instance.getClass().getDeclaredField(name);
            field.setAccessible(true);
            result = field.get(instance);
            field.setAccessible(false);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;
    }

    private Field connectionField, channelField;

    private Channel getChannel(final ServerGamePacketListenerImpl playerConnection) {
        try {
            if (connectionField == null) {
                connectionField = ServerGamePacketListenerImpl.class.getDeclaredField("h");
                connectionField.setAccessible(true);
            }
            if (channelField == null) {
                channelField = Connection.class.getDeclaredField("m");
                channelField.setAccessible(true);
            }
            return (Channel) channelField.get(connectionField.get(playerConnection));
        } catch (final NoSuchFieldException | IllegalAccessException exception) {
            throw new RuntimeException(exception);
        }
    }
}