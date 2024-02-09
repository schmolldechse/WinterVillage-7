package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Material;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Item_Heilstation extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @Override
    public String getName() {
        //return "§aHeilstation";
        return "§aKeks Bäckerei";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.BEACON, 1, this.getName())
                .lore("",
                        "§7Spieler, die im Umfeld der",
                        this.getName() + " §7stehen,",
                        "§7werden geheilt",
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
        return 3;
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().contains(Material.BEACON);
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().addItem(new ItemBuilder(Material.BEACON, 1, this.getName())
                .lore("",
                        "§7Spieler, die im Umfeld der",
                        this.getName() + " §7stehen,",
                        "§7werden geheilt")
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

            if (!this.plugin.roleManager.isPlayerAssigned(player, 1)) return;

            if (event.getClickedBlock() == null) return;

            player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);

            AreaEffectCloud areaEffectCloud = (AreaEffectCloud) event.getClickedBlock().getWorld().spawnEntity(event.getClickedBlock().getLocation().add(0, 1, 0), EntityType.AREA_EFFECT_CLOUD);
            areaEffectCloud.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 11 * 20, 2, true, true), true);
            areaEffectCloud.setDuration(15 * 20);
            areaEffectCloud.setRadius(2.5F);
            areaEffectCloud.setWaitTime(1);
        }
    }
}