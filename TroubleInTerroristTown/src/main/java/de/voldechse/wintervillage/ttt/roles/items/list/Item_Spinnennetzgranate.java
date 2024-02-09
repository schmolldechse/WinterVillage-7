package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class Item_Spinnennetzgranate extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    public static List<Location> PLACED_COBWEBS = new ArrayList<>();

    @Override
    public String getName() {
        //return "§bSpinnennetzgranate";
        return "§bAnti Mr. Frost Granate";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.COBWEB, 1, this.getName())
                .lore("",
                        "§7Diese Granate erschafft",
                        "§7am Aufschlagspunkt, in",
                        "§7ihrem Umfeld, Spinnennetze",
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
        player.getInventory().addItem(new ItemBuilder(Material.COBWEB, 1, this.getName())
                .lore("",
                        "§7Diese Granate erschafft",
                        "§7am Aufschlagspunkt, in",
                        "§7ihrem Umfeld, Spinnennetze")
                .build());
    }

    @Override
    public int howOftenBuyable() {
        return 2;
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.COBWEB);
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

            if (!this.plugin.roleManager.isPlayerAssigned(player, 1)) return;

            if (event.getAction() == Action.LEFT_CLICK_AIR
                    || event.getAction() == Action.LEFT_CLICK_BLOCK
                    || event.getAction() == Action.PHYSICAL) return;

            Location location = player.getEyeLocation().add(player.getLocation().getDirection());

            ItemStack item = event.getPlayer().getItemInHand().clone();
            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);

            Snowball snowball = player.getWorld().spawn(location, Snowball.class);
            snowball.setItem(item);
            snowball.setVelocity(player.getLocation().getDirection().multiply(2));

            this.plugin.setMetadata(snowball, "GRENADE", true);
        }
    }

    @EventHandler
    public void execute(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Snowball snowball && snowball.hasMetadata("GRENADE")) {
            Location hitLocation = null;
            if (event.getHitBlock() != null) hitLocation = event.getHitBlock().getLocation();
            if (event.getHitEntity() != null) hitLocation = event.getHitEntity().getLocation();

            snowball.remove();

            List<Block> sphere = getSphere(hitLocation.add(0, 1, 0), 4, false);
            sphere.forEach(blocks -> {
                if (blocks.getType() == Material.AIR) blocks.setType(Material.COBWEB);
                PLACED_COBWEBS.add(blocks.getLocation());
            });

            new BukkitRunnable() {
                @Override
                public void run() {
                    sphere.forEach(block -> {
                        if (block.getType() == Material.COBWEB) block.setType(Material.AIR);
                        if (PLACED_COBWEBS.contains(block.getLocation())) PLACED_COBWEBS.remove(block.getLocation());
                    });
                }
            }.runTaskLater(this.plugin.getInstance(), 30 * 20);
        }
    }

    private List<Block> getSphere(Location location, int radius, boolean empty) {
        List<Block> blocks = new ArrayList<>();
        int blockX = location.getBlockX();
        int blockY = location.getBlockY();
        int blockZ = location.getBlockZ();

        for (int x = blockX - radius; x <= blockX + radius; x++) {
            for (int y = blockY - radius; y <= blockY + radius; y++) {
                for (int z = blockZ - radius; z <= blockZ + radius; z++) {
                    double distance = ((blockX - x) * (blockX - x) + (blockZ - z) * (blockZ - z) + (blockY - y) * (blockY - y));
                    if (distance < radius * radius && (!empty && distance < (radius - 1) * (radius - 1)))
                        blocks.add(new Location(location.getWorld(), x, y, z).getBlock());
                }
            }
        }
        return blocks;
    }
}