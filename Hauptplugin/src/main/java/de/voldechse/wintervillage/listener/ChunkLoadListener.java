package de.voldechse.wintervillage.listener;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;

public class ChunkLoadListener implements Listener {

    @EventHandler
    public void execute(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();

        if (chunk.getWorld().getName().equalsIgnoreCase("world")) {
            updateBiome(chunk, Biome.SNOWY_TAIGA);
            //chunk.getWorld().setBiome(event.getChunk().getX(), event.getChunk().getZ(), Biome.SNOWY_TAIGA);
            chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
        }
    }

    private void updateBiome(Chunk chunk, Biome biome) {
        World world = chunk.getWorld();
        for (int x = chunk.getX() * 16; x < (chunk.getX() + 1) * 16; x++) {
            for (int z = chunk.getZ() * 16; z < (chunk.getZ() + 1) * 16; z++) {
                world.setBiome(x, 0, z, biome);
            }
        }
    }
}