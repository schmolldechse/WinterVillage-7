package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

public class Item_Blasrohr extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @Override
    public String getName() {
        return "§2Blasrohr";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.BAMBOO, 1, this.getName())
                .lore("",
                        "§7Schieße 5 Schuss giftiger Munition",
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
    public int howOftenBuyable() {
        return 2;
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.BAMBOO);
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.BAMBOO, 1, this.getName())
                .lore("",
                        "§7Schieße 5 Schuss giftiger Munition")
                .build());
        this.plugin.setMetadata(player, "BLASROHR", 5);

        player.setLevel(5);
        player.setExp((float) 5 / 5);
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

            if (!player.hasMetadata("BLASROHR")) {
                player.sendMessage(this.plugin.serverPrefix + "§cSomething seems to be not working. Try again");
                return;
            }

            int shotsLeft = player.getMetadata("BLASROHR").get(0).asInt();
            shotsLeft -= 1;
            this.plugin.setMetadata(player, "BLASROHR", shotsLeft);

            Arrow arrow = player.getWorld().spawnArrow(player.getEyeLocation(), player.getLocation().getDirection().multiply(3), 1.3f, 6);
            arrow.setBasePotionData(new PotionData(PotionType.POISON));
            arrow.setShooter(player);

            if (shotsLeft == 0 && player.getInventory().contains(Material.BAMBOO)) {
                player.playSound(player.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0f, 1.0f);
                player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
                this.plugin.removeMetadata(player, "BLASROHR");

                player.setLevel(0);
                player.setExp(0);
                return;
            }

            player.setLevel(shotsLeft);
            player.setExp((float) shotsLeft / 5);
        }
    }
}