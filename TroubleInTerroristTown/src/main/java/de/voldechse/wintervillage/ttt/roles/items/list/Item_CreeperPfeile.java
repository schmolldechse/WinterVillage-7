package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Item_CreeperPfeile extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    public static List<Creeper> CREEPER_LIST = new ArrayList<>();

    @Override
    public String getName() {
        //return "§aCreeper-Pfeile";
        return "§aExplosives Geschenk";
    }

    @Override
    public int getNeededPoints() {
        return 2;
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.CREEPER_SPAWN_EGG, 5, this.getName())
                .lore("",
                        "§7Schieße Pfeile und",
                        "§7spawne Creeper an",
                        "§7der Stelle, an der",
                        "§7die Pfeile aufkommen")
                .build());
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.CREEPER_SPAWN_EGG, 5, this.getName())
                .lore("",
                        "§7Schieße Pfeile und",
                        "§7spawne Creeper an",
                        "§7der Stelle, an der",
                        "§7die Pfeile aufkommen",
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
        return player.getInventory().contains(Material.CREEPER_SPAWN_EGG);
    }

    @EventHandler
    public void execute(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Arrow && event.getEntity().getShooter() instanceof Player shooter) {
            if (shooter.hasMetadata("CREEPER_ARROW")) {
                this.plugin.removeMetadata(shooter, "CREEPER_ARROW");
                this.plugin.setMetadata(event.getEntity(), "CREEPER_ARROW", true);
            }
        }
    }

    @EventHandler
    public void execute(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow arrow && arrow.hasMetadata("CREEPER_ARROW")) {

            Location hit = null;
            if (event.getHitBlock() != null) hit = event.getHitBlock().getLocation();
            if (event.getHitEntity() != null) hit = event.getHitEntity().getLocation();

            for (int i = 0; i <= 2; i++) {
                Location spawnI = getOffsetLocation(hit, 5).add(0, 1, 0);

                Creeper creeper = spawnI.getWorld().spawn(spawnI, Creeper.class);
                creeper.setExplosionRadius(4);
                creeper.setCollidable(true);

                this.plugin.setMetadata(creeper, "SUMMONED_BY", ((Player) arrow.getShooter()).getUniqueId());

                CREEPER_LIST.add(creeper);
            }

            event.getEntity().remove();
            return;
        }
    }

    @EventHandler
    public void execute(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (this.plugin.gameStateManager.currentGameState().getGameStatePhase() != Types.INGAME) return;

        if (check(player)) {
            ItemStack creeperEgg = find(player, Material.CREEPER_SPAWN_EGG);
            creeperEgg.setAmount(creeperEgg.getAmount() - 1);

            event.setConsumeItem(false);
            player.updateInventory();

            this.plugin.setMetadata(player, "CREEPER_ARROW", true);
        }
    }

    @EventHandler
    public void execute(CreatureSpawnEvent event) {
        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.SPAWNER_EGG
                && event.getEntity().getType() == EntityType.CREEPER) event.setCancelled(true);
    }

    @EventHandler
    public void execute(EntityDeathEvent event) {
        if (event.getEntity() instanceof Creeper) {
            event.setDroppedExp(0);
            event.getDrops().clear();
        }
    }

    private Location getOffsetLocation(Location hit, double offset) {
        Random random = new Random();

        double randomX = (random.nextDouble() * 2 - 1) * offset;
        double randomZ = (random.nextDouble() * 2 - 1) * offset;

        return new Location(hit.getWorld(), hit.getX() + randomX, hit.getY(), hit.getZ() + randomZ);
    }

    private ItemStack find(Player player, Material material) {
        for (ItemStack itemStack : player.getInventory().getContents())
            if (itemStack != null && itemStack.getType() == material) return itemStack;
        return null;
    }

    private boolean check(Player player) {
        ItemStack[] inventory = player.getInventory().getContents();
        int creeperEggIndex = -1;
        int arrowIndex = -1;

        for (int i = 0; i < inventory.length; i++) {
            ItemStack itemStack = inventory[i];
            if (itemStack == null) continue;

            if (itemStack.getType() == Material.CREEPER_SPAWN_EGG && creeperEggIndex == -1) creeperEggIndex = i;
            else if (itemStack.getType() == Material.ARROW && arrowIndex == -1) arrowIndex = i;

            if (creeperEggIndex != -1 && arrowIndex != -1) break;
        }

        return creeperEggIndex != -1 && arrowIndex != -1 && creeperEggIndex < arrowIndex;
    }
}