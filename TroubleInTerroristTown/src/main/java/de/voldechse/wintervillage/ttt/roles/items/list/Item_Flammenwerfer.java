package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import jline.internal.Nullable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Item_Flammenwerfer extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @Override
    public String getName() {
        //return "§6Flammenwerfer";
        return "§6Krampus Feuerrute";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.BLAZE_ROD, 1, this.getName())
                .lore("",
                        "§7Schieße einen Feuerstrahl",
                        "§7der Gegner brennen lässt",
                        "",
                        "§ePreis: " + (traitor ? "§c" : "§a") + this.getNeededPoints() + " Punkte",
                        "",
                        "§a<Klicke zum Kaufen>")
                .build();
    }

    @Override
    public int getNeededPoints() {
        return 3;
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.BLAZE_ROD, 1, this.getName())
                .lore("",
                        "§7Schieß einen Feuerstrahl",
                        "§7der Gegner brennen lässt")
                .build());
        this.plugin.setMetadata(player, "FLAMMENWERFER", 10);

        player.setLevel(10);
        player.setExp((float) 10 / 10);
    }

    @Override
    public int howOftenBuyable() {
        return 2;
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.BLAZE_ROD);
    }

    @EventHandler
    public void execute(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getItem() != null
                && event.getItem().getItemMeta().getDisplayName() != null
                && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(this.getName())) {
            event.setCancelled(true);

            if (this.plugin.gameManager.isSpectator(player)) return;

            if (this.plugin.roleManager.getRole(player) == null) return;

            //if (!this.plugin.roleManager.isPlayerAssigned(player, 2)) return;

            if (!player.hasMetadata("FLAMMENWERFER")) {
                player.sendMessage(this.plugin.serverPrefix + "§cSomething seems to be not working. Try again");
                return;
            }

            if (event.getAction() != Action.RIGHT_CLICK_AIR) return;

            int shotsLeft = player.getMetadata("FLAMMENWERFER").get(0).asInt();
            shotsLeft -= 1;
            this.plugin.setMetadata(player, "FLAMMENWERFER", shotsLeft);

            Location startLocation = getRightEyeSide(player.getEyeLocation(), 0.5).add(getRightEyeSide(player.getEyeLocation(), 0.5).getDirection().normalize().multiply(1));

            spawnParticleAlongLine(
                    player,
                    startLocation,
                    player.getEyeLocation().clone().add(player.getEyeLocation().getDirection().normalize().multiply(10)),
                    Particle.FLAME,
                    75,
                    6,
                    0.15D,
                    0.15D,
                    0.15D,
                    0D,
                    null,
                    true,
                    location -> location.getBlock().isPassable()
            );


            if (shotsLeft == 0 && player.getInventory().contains(Material.BLAZE_ROD)) {
                player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
                player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
                this.plugin.removeMetadata(player, "FLAMMENWERFER");

                player.setLevel(0);
                player.setExp(0);
                return;
            }

            player.setLevel(shotsLeft);
            player.setExp((float) shotsLeft / 10);
        }
    }

    private void spawnParticleAlongLine(
            Player player,
            Location start,
            Location end,
            Particle particle,
            int pointsPerLine,
            int particleCount,
            double offsetX,
            double offsetY,
            double offsetZ,
            double extra,
            @Nullable Double data,
            boolean forceDisplay,
            @Nullable Predicate<Location> operationPerPoint) {
        double distance = start.distance(end) / pointsPerLine;

        for (int i = 0; i < pointsPerLine; i++) {
            Location location = start.clone();

            Vector direction = end.toVector().subtract(start.toVector()).normalize();
            Vector vector = direction.multiply(i * distance);

            location.add(vector.getX(), vector.getY(), vector.getZ());

            getNearPlayers(location, 1.5).forEach(nearby -> {
                if (nearby.getUniqueId() != player.getUniqueId()) {
                    this.plugin.setMetadata(nearby, "LAST_DAMAGER", player.getUniqueId());
                    nearby.setFireTicks(7 * 20);
                }
            });

            if (operationPerPoint == null) {
                start.getWorld().spawnParticle(particle, location, particleCount, offsetX, offsetY, offsetZ, extra, data, forceDisplay);
                continue;
            }

            if (operationPerPoint.test(location))
                start.getWorld().spawnParticle(particle, location, particleCount, offsetX, offsetY, offsetZ, extra, data, forceDisplay);
        }
    }

    private Location getRightEyeSide(Location location, double distance) {
        float angle = location.getYaw() / 60;
        return location.clone().subtract(new Vector(Math.cos(angle), 0, Math.sin(angle)).normalize().multiply(distance)).subtract(0, 0.4, 0);
    }

    private List<Player> getNearPlayers(Location location, double radius) {
        List<Player> nearbyPlayers = new ArrayList<>();
        double radiusSquared = radius * radius;

        this.plugin.roleManager.getPlayerList().forEach(player -> {
            if (player.getLocation().distance(location) <= radiusSquared) nearbyPlayers.add(player);
        });

        return nearbyPlayers;
    }
}