package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import de.voldechse.wintervillage.ttt.utils.position.PositionEntity;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Item_ZweiteChance extends RoleItem implements Listener {

    private static final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @Override
    public String getName() {
        //return "§bZweite Chance";
        return "§bNordlicht";
    }

    @Override
    public int getNeededPoints() {
        return 5;
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.TOTEM_OF_UNDYING, 1, this.getName())
                .lore("",
                        "§7Lege eine Position fest,",
                        "§7um dich vor einem sicheren",
                        "§7Tod zu retten",
                        "",
                        "§ePreis: " + (traitor ? "§c" : "§a") + this.getNeededPoints() + " Punkte",
                        "",
                        "§a<Klicke zum Kaufen>")
                .build();
    }

    @Override
    public int howOftenBuyable() {
        return 2;
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.TOTEM_OF_UNDYING) || player.hasMetadata("SAVED_RESPAWN_POSITION");
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.TOTEM_OF_UNDYING, 1, this.getName())
                .lore("",
                        "§7Lege eine Position fest,",
                        "§7um dich vor einem sicheren",
                        "§7Tod zu retten")
                .build());
    }

    @EventHandler
    public void execute(EntityResurrectEvent event) {
        if (event.getEntity() instanceof Player
                && event.getEntity().hasMetadata("SAVED_RESPAWN_POSITION")
                && event.getEntity().hasMetadata("TOTEM_TASKID")) {

            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin.getInstance(), new Runnable() {
                @Override
                public void run() {
                    Location location = (Location) event.getEntity().getMetadata("SAVED_RESPAWN_POSITION").get(0).value();

                    event.getEntity().teleport(location);
                    event.getEntity().setHealth(((Player) event.getEntity()).getHealthScale() / 2);

                    int taskId = event.getEntity().getMetadata("TOTEM_TASKID").get(0).asInt();
                    Bukkit.getScheduler().cancelTask(taskId);

                    plugin.removeMetadata(event.getEntity(), "TOTEM_TASKID");
                    plugin.removeMetadata(event.getEntity(), "SAVED_RESPAWN_POSITION");
                }
            }, 1L);
        }
    }

    @EventHandler
    public void execute(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player
                && event.getFinalDamage() > player.getHealth()
                && player.hasMetadata("SAVED_RESPAWN_POSITION")) {
            ItemStack offHand = player.getInventory().getItemInOffHand();
            if (offHand.getType() == Material.AIR) {
                player.getInventory().setItemInOffHand(new ItemBuilder(Material.TOTEM_OF_UNDYING).build());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.getInventory().setItemInOffHand(new ItemBuilder(Material.AIR).build());
                    }
                }.runTaskLater(this.plugin.getInstance(), 10L);
            } else {
                player.getInventory().setItemInOffHand(new ItemBuilder(Material.TOTEM_OF_UNDYING).build());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        player.getInventory().setItemInOffHand(offHand);
                    }
                }.runTaskLater(this.plugin.getInstance(), 10L);
            }
        }
    }

    public static void startTotemTask(Player player, Location location) {
        BukkitRunnable totemTask = new BukkitRunnable() {
            double v = 0;

            Location first, second;

            @Override
            public void run() {

                v += Math.PI / 16;
                first = location.clone().add(Math.cos(v), Math.sin(v) + 1, Math.sin(v));
                second = location.clone().add(Math.cos(v + Math.PI), Math.sin(v) + 1, Math.sin(v + Math.PI));

                player.spawnParticle(
                        Particle.TOTEM,
                        first.getX(),
                        first.getY(),
                        first.getZ(),
                        0,
                        0,
                        0,
                        0,
                        1,
                        null
                );

                player.spawnParticle(
                        Particle.TOTEM,
                        second.getX(),
                        second.getY(),
                        second.getZ(),
                        0,
                        0,
                        0,
                        0,
                        1,
                        null
                );

                /**
                ClientboundLevelParticlesPacket packet_1 = new ClientboundLevelParticlesPacket(
                        ParticleTypes.TOTEM_OF_UNDYING,
                        true,
                        first.getX(),
                        first.getY(),
                        first.getZ(),
                        0,
                        0,
                        0,
                        1,
                        0
                );
                ClientboundLevelParticlesPacket packet_2 = new ClientboundLevelParticlesPacket(
                        ParticleTypes.TOTEM_OF_UNDYING,
                        true,
                        second.getX(),
                        second.getY(),
                        second.getZ(),
                        0,
                        0,
                        0,
                        1,
                        0
                );

                ((CraftPlayer) player).getHandle().connection.send(packet_1);
                ((CraftPlayer) player).getHandle().connection.send(packet_2);
                 */
            }
        };
        totemTask.runTaskTimer(plugin.getInstance(), 0L, 1L);

        plugin.setMetadata(player, "TOTEM_TASKID", totemTask.getTaskId());
    }
}