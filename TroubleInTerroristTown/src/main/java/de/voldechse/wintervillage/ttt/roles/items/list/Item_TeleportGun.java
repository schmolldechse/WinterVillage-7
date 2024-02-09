package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;

import java.util.Arrays;
import java.util.Random;

public class Item_TeleportGun extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @Override
    public String getName() {
        return "§aTeleport-Gun";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        ItemStack crossbow = new ItemBuilder(Material.CROSSBOW, 1, this.getName()).build();
        CrossbowMeta crossbowMeta = (CrossbowMeta) crossbow.getItemMeta();
        crossbowMeta.addChargedProjectile(new ItemBuilder(Material.FIREWORK_ROCKET, 1).build());
        crossbowMeta.setLore(Arrays.asList(
                "",
                "§7Teleportiere deine Gegner",
                "§7durch die Welt",
                "",
                "§ePreis: " + (traitor ? "§c" : "§a") + this.getNeededPoints() + " Punkte",
                "",
                "§a<Klicke zum Kaufen>")
        );
        crossbowMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true);
        crossbowMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        crossbow.setItemMeta(crossbowMeta);
        return crossbow;
    }

    @Override
    public int getNeededPoints() {
        return 3;
    }

    @Override
    public void equipItems(Player player) {
        ItemStack crossbow = new ItemBuilder(Material.CROSSBOW, 1, this.getName()).build();
        crossbow.setDurability((short) 464);
        CrossbowMeta crossbowMeta = (CrossbowMeta) crossbow.getItemMeta();
        crossbowMeta.addChargedProjectile(new ItemBuilder(Material.FIREWORK_ROCKET, 1).build());
        crossbowMeta.setLore(Arrays.asList(
                "",
                "§7Teleportiere deine Gegner",
                "§7durch die Welt")
        );
        crossbowMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2, true);
        crossbowMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        crossbow.setItemMeta(crossbowMeta);

        player.getInventory().addItem(crossbow);
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.CROSSBOW);
    }

    @Override
    public int howOftenBuyable() {
        return 3;
    }

    @EventHandler
    public void execute(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Firework firework
                && firework.hasMetadata("TELEPORT_GUN")
                && event.getHitEntity() != null
                && event.getHitEntity() instanceof Player hit) {
            Player shooter = (Player) firework.getShooter();

            event.getEntity().remove();

            hit.teleport(getHighest(hit.getLocation(), 50));
            this.plugin.gameManager.playSound(Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR, 1.0f);
            this.plugin.setMetadata(hit, "LAST_DAMAGER", shooter.getUniqueId());
        }
    }

    @EventHandler
    public void execute(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        if (this.plugin.gameStateManager.currentGameState().getGameStatePhase() != Types.INGAME) return;

        if (event.getBow().getType() == Material.CROSSBOW
                && event.getBow().getItemMeta() != null
                && event.getBow().getItemMeta().getDisplayName().equalsIgnoreCase(this.getName()))
            this.plugin.setMetadata(event.getProjectile(), "TELEPORT_GUN", true);
    }

    private Location getHighest(Location location, int radius) {
        Random random = new Random();

        int x = location.getBlockX() + random.nextInt(2 * radius) - radius;
        int z = location.getBlockZ() + random.nextInt(2 * radius) - radius;

        int y = location.getWorld().getHighestBlockYAt(x, z);

        return new Location(location.getWorld(), x, y, z);
    }
}