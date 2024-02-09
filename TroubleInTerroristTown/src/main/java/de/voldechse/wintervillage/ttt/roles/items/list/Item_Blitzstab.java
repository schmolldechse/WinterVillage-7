package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class Item_Blitzstab extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @Override
    public String getName() {
        return "§eBlitzstab";
    }

    @Override
    public int getNeededPoints() {
        return 3;
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.GOLDEN_AXE, 1, this.getName())
                .lore("",
                        "§7Benutze den Blitzstab,",
                        "§7um deine Gegner mit den",
                        "§7Blitzen Zeus' anzugreifen",
                        "",
                        "§ePreis: " + (traitor ? "§c" : "§a") + this.getNeededPoints() + " Punkte",
                        "",
                        "§a<Klicke zum Kaufen>")
                .build();
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.GOLDEN_AXE);
    }

    @Override
    public int howOftenBuyable() {
        return 3;
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.GOLDEN_AXE, 1, this.getName())
                .lore("",
                        "§7Benutze den Blitzstab,",
                        "§7um deine Gegner mit den",
                        "§7Blitzen Zeus' anzugreifen")
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

            ItemStack axe = player.getInventory().getItemInHand();
            short newDurability = (short) (axe.getDurability() + 0.1 * axe.getType().getMaxDurability());

            if (newDurability < axe.getType().getMaxDurability()) {
                axe.setDurability(newDurability);
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                axe.setAmount(axe.getAmount() - 1);
                return;
            }

            Location lookingAt = player.getTargetBlock((Set<Material>) null, 30).getLocation();

            LightningStrike lightningStrike = lookingAt.getWorld().strikeLightning(lookingAt);
            this.plugin.setMetadata(lightningStrike, "SUMMONED_BY", player.getUniqueId());
        }
    }
}