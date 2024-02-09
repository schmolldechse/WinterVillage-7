package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Item_Landmine extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    public static List<Location> MINE = new ArrayList<>();

    @Override
    public String getName() {
        return "§6Landmine";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.BARRIER, 1, this.getName())
                .lore("",
                        "§7Platziere die Landmine",
                        "§7um deine Gegner aus dem Boden",
                        "§7zu überraschen",
                        "",
                        "§ePreis: " + (traitor ? "§c" : "§a") + this.getNeededPoints() + " Punkte",
                        "",
                        "§a<Klicke zum Kaufen>")
                .build();
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.STONE_PRESSURE_PLATE, 1, this.getName())
                .lore("",
                        "§7Platziere die Landmine",
                        "§7um deine Gegner aus dem Boden",
                        "§7zu überraschen")
                .build());
    }

    @Override
    public int getNeededPoints() {
        return 2;
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return false;
    }

    @Override
    public int howOftenBuyable() {
        return 4;
    }

    @EventHandler
    public void execute(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (event.getItemInHand() != null
                && event.getItemInHand().getItemMeta().getDisplayName() != null
                && event.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase(this.getName())) {
            if (this.plugin.gameManager.isSpectator(player)) {
                event.setCancelled(true);
                return;
            }

            if (this.plugin.roleManager.getRole(player) == null) {
                event.setCancelled(true);
                return;
            }

            if (!this.plugin.roleManager.isPlayerAssigned(player, 2)) {
                event.setCancelled(true);
                return;
            }

            MINE.add(event.getBlock().getLocation());

            event.getBlock().setMetadata("MINE", new FixedMetadataValue(this.plugin.getInstance(), true));
            event.getBlock().setMetadata("SUMMONED_BY", new FixedMetadataValue(this.plugin.getInstance(), player.getUniqueId()));

            event.setCancelled(false);
        }
    }

    @EventHandler
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.PHYSICAL
                && event.getClickedBlock().getType() == Material.STONE_PRESSURE_PLATE
                && event.getClickedBlock().hasMetadata("MINE")) {
            if (this.plugin.gameManager.isSpectator(player)) {
                event.setCancelled(true);
                return;
            }

            if (this.plugin.roleManager.getRole(player) == null) {
                event.setCancelled(true);
                return;
            }

            if (this.plugin.roleManager.isPlayerAssigned(player, 2)) {
                event.setCancelled(true);
                return;
            }

            Player placer = Bukkit.getPlayer(UUID.fromString(event.getClickedBlock().getMetadata("SUMMONED_BY").get(0).asString()));

            TNTPrimed tnt = event.getClickedBlock().getWorld().spawn(event.getClickedBlock().getLocation(), TNTPrimed.class);
            tnt.setGravity(true);
            tnt.setIsIncendiary(false);
            tnt.setFuseTicks(1);
            tnt.setYield(3F);
            if (this.plugin.roleManager.isPlayerAssigned(placer)) tnt.setSource(placer);

            this.plugin.setMetadata(tnt, "SUMMONED_BY", player.getUniqueId());

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (MINE.contains(event.getClickedBlock().getLocation()))
                        MINE.remove(event.getClickedBlock().getLocation());
                    event.getClickedBlock().setType(Material.AIR);
                }
            }.runTaskLater(this.plugin.getInstance(), 10L);
        }
    }
}