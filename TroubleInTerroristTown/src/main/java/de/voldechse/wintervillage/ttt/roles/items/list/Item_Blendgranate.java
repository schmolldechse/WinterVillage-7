package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class Item_Blendgranate extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @Override
    public String getName() {
        return "§7Blendgranate";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.FIREWORK_STAR, 1, this.getName())
                .lore("",
                        "§7Diese Granate erschafft",
                        "§7am Aufschlagspunkt, in",
                        "§7ihrem Umfeld, einen Blendeffekt",
                        "",
                        "§ePreis: " + (traitor ? "§c" : "§a") + this.getNeededPoints() + " Punkte",
                        "",
                        "§a<Klicke zum Kaufen>")
                .build();
    }

    @Override
    public int howOftenBuyable() {
        return 3;
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.FIREWORK_STAR);
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.FIREWORK_STAR, 1, this.getName())
                .lore("",
                        "§7Diese Granate erschafft",
                        "§7am Aufschlagspunkt, in",
                        "§7ihrem Umfeld, einen Blendeffekt")
                .build());
    }

    @Override
    public int getNeededPoints() {
        return 2;
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

            if (event.getAction() == Action.LEFT_CLICK_AIR
                    || event.getAction() == Action.LEFT_CLICK_BLOCK
                    || event.getAction() == Action.PHYSICAL) return;

            Location location = player.getEyeLocation().add(player.getLocation().getDirection());

            ItemStack item = event.getPlayer().getItemInHand().clone();
            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);

            Snowball snowball = player.getWorld().spawn(location, Snowball.class);
            snowball.setItem(item);
            snowball.setVelocity(player.getLocation().getDirection().multiply(2));
            snowball.setShooter(player);

            this.plugin.setMetadata(snowball, "SMOKE", true);
        }
    }

    @EventHandler
    public void execute(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball snowball && snowball.hasMetadata("SMOKE")) {

            Location hitLocation = null;
            if (event.getHitBlock() != null) hitLocation = event.getHitBlock().getLocation();
            if (event.getHitEntity() != null) hitLocation = event.getHitEntity().getLocation();

            snowball.remove();

            Player shooter = (Player) event.getEntity().getShooter();

            Location finalHitLocation = hitLocation;
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(this.plugin.getInstance(), new Runnable() {
                @Override
                public void run() {
                    finalHitLocation.getWorld().playSound(finalHitLocation, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
                }
            }, 5L, 10L);

            Bukkit.getScheduler().runTaskLater(this.plugin.getInstance(), new Runnable() {
                @Override
                public void run() {
                    task.cancel();

                    finalHitLocation.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, finalHitLocation, 50, 4, 4, 4, 0D, null, true);
                    finalHitLocation.getWorld().spawnParticle(Particle.SMOKE_LARGE, finalHitLocation, 50, 4, 4, 4, 0D, null, true);
                    finalHitLocation.getWorld().playSound(finalHitLocation, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);

                    for (Player nearby : getNearPlayers(finalHitLocation, 5)) {
                        if (nearby.getUniqueId().equals(shooter.getUniqueId())) continue;
                        nearby.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10 * 20, 2, true, true));
                        nearby.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 9 * 20, 2, true, true));
                    }
                }
            }, 40L);

            snowball.remove();
        }
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