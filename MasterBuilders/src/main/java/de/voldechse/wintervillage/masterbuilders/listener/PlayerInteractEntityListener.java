package de.voldechse.wintervillage.masterbuilders.listener;

import de.voldechse.wintervillage.masterbuilders.MasterBuilders;
import de.voldechse.wintervillage.masterbuilders.gamestate.Types;
import de.voldechse.wintervillage.masterbuilders.teams.Team;
import de.voldechse.wintervillage.masterbuilders.utils.RectangleManager;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractEntityListener implements Listener {

    private final MasterBuilders plugin = InjectionLayer.ext().instance(MasterBuilders.class);

    @EventHandler
    public void execute(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (event.getRightClicked().getType() == EntityType.VILLAGER && event.getRightClicked().getCustomName().equalsIgnoreCase("§6Rechtsklick mich")) {
            event.setCancelled(true);
            if (this.plugin.gameManager.isSpectator(player)) return;

            if (!this.plugin.gameStateManager.currentGameState().getGameStatePhase().equals(Types.BUILDING_PHASE))
                return;

            if (player.getItemInHand().getType() == Material.AIR) {
                player.sendMessage(this.plugin.serverPrefix + "§aRechtsklicke mich mit einem Block deiner Wahl um den Boden zu verändern!");
                return;
            }

            if (!isBlock(player.getItemInHand())) return;

            Team plot = this.plugin.teamManager.getTeam(player);
            Location cornerA = new Location(
                    Bukkit.getWorld(plot.cornerA.getWorld()),
                    plot.cornerA.getX(),
                    plot.cornerA.getY(),
                    plot.cornerA.getZ()
            );
            Location cornerB = new Location(
                    Bukkit.getWorld(plot.cornerB.getWorld()),
                    plot.cornerB.getX(),
                    plot.cornerB.getY(),
                    plot.cornerB.getZ()
            );

            RectangleManager.fillRectangle(cornerA, cornerB, player.getItemInHand().getType());

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.0F);
        }
    }

    private boolean isBlock(ItemStack itemStack) {
        Material material = itemStack.getType();
        return material.isBlock()
                && material.isSolid()
                && !material.isInteractable()
                && !material.name().endsWith("BANNER")
                && material != Material.POINTED_DRIPSTONE
                && material != Material.SMALL_AMETHYST_BUD
                && material != Material.MEDIUM_AMETHYST_BUD
                && material != Material.LARGE_AMETHYST_BUD
                && material != Material.AMETHYST_CLUSTER
                && material != Material.CACTUS
                && material != Material.BAMBOO
                && !material.name().endsWith("EGG")
                && material != Material.SCULK_VEIN
                && material != Material.SCULK_SHRIEKER
                && material != Material.CALIBRATED_SCULK_SENSOR
                && material != Material.SCULK_SENSOR
                && material != Material.CHAIN
                && material != Material.IRON_BARS
                && !material.name().contains("LANTERN")
                && material != Material.LIGHTNING_ROD
                && material != Material.DECORATED_POT
                && material != Material.END_PORTAL_FRAME
                && material != Material.SPAWNER
                && material != Material.CONDUIT
                && !material.name().contains("LEAVES")
                && !material.name().contains("GLASS_PANE")
                && !material.name().contains("PRESSURE_PLATE")
                && !material.name().contains("WALL")
                && material != Material.BARRIER;
    }
}