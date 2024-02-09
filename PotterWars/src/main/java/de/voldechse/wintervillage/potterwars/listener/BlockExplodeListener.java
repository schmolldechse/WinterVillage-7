package de.voldechse.wintervillage.potterwars.listener;

import de.voldechse.wintervillage.potterwars.PotterWars;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BlockExplodeListener implements Listener {

    @EventHandler
    public void execute(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.CHEST) {
            if (BlockPlaceListener.blockedChestLocation.contains(event.getBlock().getLocation())) return;
            //TODO: add this function later again
            //if (PotterWarsGame.getInstance().isPotterWarsChest(event.getBlock().getLocation())) {
            for (ItemStack item : PotterWars.getInstance().chestManager.getAt(event.getBlock().getLocation()).getInventory().getContents())
                if (item != null)
                    event.getBlock().getLocation().getWorld().dropItem(event.getBlock().getLocation().add(0, 1, 0), item);
            //PotterWarsGame.getInstance().getChestManager().getAt(event.getBlock().getLocation()).getInventory().clear();
            PotterWars.getInstance().chestManager.getAt(event.getBlock().getLocation()).clearInventory();
            //}
            return;
        }
    }
}