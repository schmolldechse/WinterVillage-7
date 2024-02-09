package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.document.Document;
import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import net.minecraft.world.entity.Entity;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

public class Item_Bumerang extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @Override
    public String getName() {
        return "§3Bumerang";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.TRIDENT, 1, this.getName())
                .lore("",
                        "§7Ein Dreizack, den du 3 Mal werfen",
                        "§7kannst und der zu dir zurückkehrt.",
                        "§7Auf seinem Flug fügt er",
                        "§7dem Gegner Schaden zu",
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
    public int howOftenBuyable() {
        return 2;
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.TRIDENT);
    }

    @Override
    public void equipItems(Player player) {
        ItemStack itemStack = new ItemStack(Material.TRIDENT);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(this.getName());
        itemMeta.addEnchant(Enchantment.LOYALTY, 3, false);
        itemMeta.setLore(Arrays.asList("",
                "§7Ein Dreizack, den du 3 Mal werfen",
                "§7kannst und der zu dir zurückkehrt.",
                "§7Auf seinem Flug fügt er",
                "§7dem Gegner Schaden zu"));
        itemStack.setItemMeta(itemMeta);

        player.getInventory().addItem(itemStack);
    }

    @EventHandler
    public void execute(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Trident) {
            Player shooter = (Player) event.getEntity().getShooter();

            int used = 0;
            Document metadata = null;

            if (shooter.hasMetadata("BUMERANG")) {
                metadata = (Document) shooter.getMetadata("BUMERANG").get(0).value();
                used = metadata.getInt("USED");
            }
            used += 1;

            this.plugin.setMetadata(event.getEntity(), "SUMMONED_BY", shooter.getUniqueId());

            Document document = new Document("USED", used)
                    .append("SHOT_BY", shooter.getUniqueId());
            this.plugin.setMetadata(event.getEntity(), "DOCUMENT", document);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!event.getEntity().isValid()) {
                        Entity entity = ((CraftEntity) event.getEntity()).getHandle();
                        entity.kill();
                        cancel();
                        return;
                    }

                    if (!plugin.roleManager.isPlayerAssigned(shooter)) {
                        Entity entity = ((CraftEntity) event.getEntity()).getHandle();
                        entity.kill();
                        cancel();
                        return;
                    }

                    for (org.bukkit.entity.Entity nearby : event.getEntity().getNearbyEntities(2.0, 2.0, 2.0)) {
                        if (!(nearby instanceof Player player)) continue;
                        if (plugin.gameManager.isSpectator(player)) continue;
                        if (player.getUniqueId().equals(shooter.getUniqueId())) continue;
                        player.damage(3D);
                    }
                }
            }.runTaskTimer(this.plugin.getInstance(), 0L, 10L);
        }
    }

    @EventHandler
    public void execute(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();

        if (this.plugin.roleManager.getRole(player) != null
                && this.plugin.roleManager.getRole(player).roleId == 2
                && event.getItem().getType() == Material.TRIDENT) {
            int damage = (event.getItem().getType().getMaxDurability() / 3) + 1;
            event.setDamage(damage);
        }
    }

    @EventHandler
    public void execute(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Trident && event.getEntity().hasMetadata("DOCUMENT")) {
            Document document = (Document) event.getEntity().getMetadata("DOCUMENT").get(0).value();

            int used = document.getInt("USED");
            if (used == 3) {
                Entity entity = ((CraftEntity) event.getEntity()).getHandle();
                entity.kill();

                if (((Player) event.getEntity().getShooter()).hasMetadata("BUMERANG"))
                    this.plugin.removeMetadata(((Player) event.getEntity().getShooter()), "BUMERANG");
                return;
            }

            this.plugin.setMetadata((Player) event.getEntity().getShooter(), "BUMERANG", document);
        }
    }
}