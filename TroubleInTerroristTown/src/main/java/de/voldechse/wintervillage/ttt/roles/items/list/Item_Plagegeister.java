package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Item_Plagegeister extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    private final Random RANDOM = new Random();

    public static List<Vex> VEX_LIST = new ArrayList<>();

    @Override
    public String getName() {
        //return "§8Plagegeister";
        return "§8Wichte";
    }

    @Override
    public int getNeededPoints() {
        return 2;
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.VEX_SPAWN_EGG, 1, this.getName())
                .lore("",
                        "§7Spawne Geister, die den Gegnern schaden",
                        "",
                        "§ePreis: " + (traitor ? "§c" : "§a") + this.getNeededPoints() + " Punkte",
                        "",
                        "§a<Klicke zum Kaufen>")
                .build();
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.VEX_SPAWN_EGG);
    }

    @Override
    public int howOftenBuyable() {
        return 1;
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.VEX_SPAWN_EGG, 1, this.getName())
                .lore("",
                        "§7Spawne Geister, die den Gegnern schaden")
                .build());
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

            if (!this.plugin.roleManager.isPlayerAssigned(player, 2)) return;

            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);

            for (int i = 0; i < 9; i++) {
                Vex vex = player.getWorld().spawn(randomOffsetLocation(player.getEyeLocation(), 4), Vex.class);
                vex.setRotation(player.getLocation().getYaw(), player.getLocation().getPitch());

                this.plugin.setMetadata(vex, "SUMMONED_BY", player.getUniqueId());

                VEX_LIST.add(vex);
            }
        }
    }

    @EventHandler
    public void execute(EntityDeathEvent event) {
        if (event.getEntity() instanceof Vex) {
            event.setDroppedExp(0);
            event.getDrops().clear();
        }
    }

    private Location randomOffsetLocation(Location location, double maxOffset) {
        double offsetX = (RANDOM.nextDouble() * 2 - 1) * maxOffset;
        double offsetY = (RANDOM.nextDouble() * 2 - 1) * maxOffset;
        double offsetZ = (RANDOM.nextDouble() * 2 - 1) * maxOffset;

        return location.clone().add(offsetX, offsetY, offsetZ);
    }
}