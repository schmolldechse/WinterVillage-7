package de.voldechse.wintervillage.listener;

import de.voldechse.wintervillage.WinterVillage;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.ArrayList;
import java.util.List;

public class BlockPlaceListener implements Listener {

    @EventHandler
    public void execute(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        WinterVillage plugin = InjectionLayer.ext().instance(WinterVillage.class);

        if (!player.isOp()
                && ((event.getBlock().getWorld().getName().equalsIgnoreCase("world_nether") && withinRadius(Bukkit.getWorld("world_nether").getSpawnLocation(), 15).contains(event.getBlock()))
                || ((event.getBlock().getWorld().getName().equalsIgnoreCase("world_farmwelt") && withinRadius(Bukkit.getWorld("world_farmwelt").getSpawnLocation(), 15).contains(event.getBlock()))
                || ((event.getBlock().getWorld().getName().equalsIgnoreCase("world_the_end") && withinRadius(Bukkit.getWorld("world_the_end").getSpawnLocation(), 5).contains(event.getBlock())))))) {
            event.setCancelled(true);
            player.sendMessage(plugin.serverPrefix + "§cDu kannst hier nicht bauen");
            return;
        }
    }

    private List<Block> withinRadius(Location center, int radius) {
        List<Block> blocks = new ArrayList<>();

        for (int xOffset = -radius; xOffset <= radius; xOffset++) {
            for (int yOffset = -radius; yOffset <= radius; yOffset++) {
                for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                    blocks.add(center.getWorld().getBlockAt(center.getBlockX() + xOffset, center.getBlockY() + yOffset, center.getBlockZ() + zOffset));
                }
            }
        }

        return blocks;
    }
}
