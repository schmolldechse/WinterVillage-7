package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Item_Fallschirm extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @Override
    public String getName() {
        return "§aFallschirm";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.PHANTOM_MEMBRANE, 3, this.getName())
                .lore("",
                        "§7Schützt dich vor Fallschaden",
                        "",
                        "§ePreis: " + (traitor ? "§c" : "§a") + this.getNeededPoints() + " Punkte",
                        "",
                        "§a<Klicke zum Kaufen>")
                .build();
    }

    @Override
    public int getNeededPoints() {
        return 2;
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.PHANTOM_MEMBRANE, 3, this.getName())
                .lore("",
                        "§7Schützt dich vor Fallschaden")
                .build());
    }

    @Override
    public int howOftenBuyable() {
        return 3;
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.PHANTOM_MEMBRANE);
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

            if (player.isOnGround()) return;

            if (player.hasMetadata("USING_PARACHUTE")) return;

            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 1, true, true, true));
            this.plugin.setMetadata(player, "USING_PARACHUTE", true);
        }
    }

    @EventHandler
    public void execute(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.hasMetadata("USING_PARACHUTE")) {
            if (!player.isOnGround()) return;
            this.plugin.removeMetadata(player, "USING_PARACHUTE");
            player.removePotionEffect(PotionEffectType.SLOW_FALLING);
        }
    }
}