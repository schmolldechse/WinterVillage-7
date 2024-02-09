package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Arrays;

public class Item_StomperBoots extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @Override
    public String getName() {
        //return "§fStomper-Boots";
        return "§fKrampus-Boots";
    }

    @Override
    public int getNeededPoints() {
        return 4;
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        ItemStack boots = new ItemBuilder(Material.LEATHER_BOOTS, 1, this.getName()).build();
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
        bootsMeta.setColor(Color.WHITE);
        bootsMeta.setLore(Arrays.asList(
                "§7Überrasche deine Gegner",
                "§7von oben!",
                "",
                "§ePreis: " + (traitor ? "§c" : "§a") + this.getNeededPoints() + " Punkte",
                "",
                "§a<Klicke zum Kaufen>")
        );
        bootsMeta.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES);
        boots.setItemMeta(bootsMeta);

        return boots;
    }

    @Override
    public void equipItems(Player player) {
        ItemStack boots = new ItemBuilder(Material.LEATHER_BOOTS, 1, this.getName()).build();
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
        bootsMeta.setColor(Color.WHITE);
        bootsMeta.setLore(Arrays.asList(
                "§7Überrasche deine Gegner",
                "§7von oben!")
        );
        bootsMeta.addItemFlags(ItemFlag.HIDE_DYE, ItemFlag.HIDE_ATTRIBUTES);
        boots.setItemMeta(bootsMeta);

        player.getInventory().setBoots(boots);
    }

    @Override
    public int howOftenBuyable() {
        return 2;
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().getBoots() != null && player.getInventory().getBoots().getType() == Material.LEATHER_BOOTS;
    }

    @EventHandler
    public void execute(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.getFallDistance() >= 4
                && player.isOnGround()
                && player.getInventory().getBoots() != null
                && player.getInventory().getBoots().getType() == Material.LEATHER_BOOTS
                && player.getNearbyEntities(3.0, 0, 3.0).size() > 0) {

            double fallHeight = player.getFallDistance();

            short newDurability = (short) (player.getInventory().getBoots().getDurability() + 0.2 * player.getInventory().getBoots().getType().getMaxDurability());
            player.getInventory().getBoots().setDurability(newDurability);

            for (Entity entity : player.getNearbyEntities(3.0, 0, 3.0)) {
                if (!(entity instanceof Player nearby)) continue;
                if (nearby.getUniqueId().equals(player.getUniqueId())) continue;
                if (this.plugin.gameManager.isSpectator(nearby)) continue;
                if (this.plugin.roleManager.isPlayerAssigned(nearby, 2)) continue;
                nearby.damage(fallHeight * 1.45);

                this.plugin.setMetadata(nearby, "LAST_DAMAGER", player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void execute(PlayerItemDamageEvent event) {
        if (event.getItem().getType() == Material.LEATHER_BOOTS
                && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(this.getName()))
            event.setDamage(0);
    }
}