package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Item_Lagerfeuer extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    public static Map<Location, BukkitRunnable> PLACED_CAMPFIRES = new HashMap<>();

    @Override
    public String getName() {
        return "§bLagerfeuer";
    }

    @Override
    public int getNeededPoints() {
        return 3;
    }

    @Override
    public int howOftenBuyable() {
        return 2;
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.SOUL_CAMPFIRE);
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.SOUL_CAMPFIRE, 1, this.getName())
                .lore("",
                        "§7Platziere das Lagerfeuer",
                        "§7um dich über Zeit selbst zu heilen",
                        "",
                        "§ePreis: " + (traitor ? "§c" : "§a") + this.getNeededPoints() + " Punkte",
                        "",
                        "§a<Klicke zum Kaufen>")
                .build();
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.SOUL_CAMPFIRE, 1, this.getName())
                .lore("",
                        "§7Platziere das Lagerfeuer",
                        "§7um dich über Zeit selbst zu heilen")
                .build());
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

            Location testerCenter = calculateCenter(this.plugin.testerSetup.cornerA, this.plugin.testerSetup.cornerB);
            if (event.getBlock().getLocation().distanceSquared(testerCenter) <= 20) {
                event.setCancelled(true);
                return;
            }

            event.setCancelled(false);

            Lightable lightable = (Lightable) event.getBlock().getBlockData();
            lightable.setLit(true);
            event.getBlock().setBlockData(lightable);

            BukkitRunnable bukkitRunnable = new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player nearby : getNearPlayers(event.getBlock().getLocation(), 4)) {
                        if ((nearby.getHealth() + .5) >= nearby.getMaxHealth()) continue;
                        nearby.setHealth(nearby.getHealth() + .5);
                    }
                }
            };
            bukkitRunnable.runTaskTimer(this.plugin.getInstance(), 0L, 10L);

            PLACED_CAMPFIRES.put(event.getBlock().getLocation(), bukkitRunnable);

            Bukkit.getScheduler().runTaskLater(this.plugin.getInstance(), new Runnable() {
                @Override
                public void run() {
                    if (!bukkitRunnable.isCancelled()) bukkitRunnable.cancel();
                    event.getBlock().setType(Material.AIR);
                    PLACED_CAMPFIRES.remove(event.getBlock().getLocation());
                }
            }, 30 * 20L);
        }
    }

    private Location calculateCenter(Location cornerA, Location cornerB) {
        double minX = Math.min(cornerA.getX(), cornerB.getX());
        double minY = Math.min(cornerA.getY(), cornerB.getY());
        double minZ = Math.min(cornerA.getZ(), cornerB.getZ());

        double maxX = Math.max(cornerA.getX(), cornerB.getX());
        double maxY = Math.max(cornerA.getY(), cornerB.getY());
        double maxZ = Math.max(cornerA.getZ(), cornerB.getZ());

        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;
        double centerZ = (minZ + maxZ) / 2.0;

        return new Location(cornerA.getWorld(), centerX, centerY, centerZ);
    }

    private List<Player> getNearPlayers(Location location, double radius) {
        List<Player> nearbyPlayers = new ArrayList<>();
        double radiusSquared = radius * radius;

        this.plugin.roleManager.getPlayerList().forEach(player -> {
            if (player.getLocation().distanceSquared(location) <= radiusSquared) nearbyPlayers.add(player);
        });

        return nearbyPlayers;
    }
}