package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class Item_OneShotBogen extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @Override
    public String getName() {
        //return "§cOne-Shot-Bogen";
        return "§cLast-Wish-Bogen";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.CROSSBOW, 1, this.getName())
                .lore("",
                        "§7Mit diesem Bogen kannst du",
                        "§7genau einen Schuss abfeuern.",
                        "§7Dieser ist dafür aber immer tödlich",
                        "",
                        "§ePreis: " + (traitor ? "§c" : "§a") + this.getNeededPoints() + " Punkte",
                        "",
                        "§a<Klicke zum Kaufen>")
                .build();
    }

    @Override
    public int getNeededPoints() {
        return 5;
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return false;
    }

    @Override
    public int howOftenBuyable() {
        return 1;
    }

    @Override
    public void equipItems(Player player) {
        ItemStack crossbow = new ItemBuilder(Material.CROSSBOW, 1, this.getName()).build();
        crossbow.setDurability((short) 464);
        ItemMeta itemMeta = crossbow.getItemMeta();
        itemMeta.setLore(Arrays.asList(
                "",
                "§7Mit diesem Bogen kannst du",
                "§7genau einen Schuss abfeuern.",
                "§7Dieser ist dafür aber immer tödlich"
        ));
        itemMeta.addEnchant(Enchantment.ARROW_DAMAGE, 200, true);
        crossbow.setItemMeta(itemMeta);

        player.getInventory().addItem(crossbow);
        player.getInventory().addItem(new ItemBuilder(Material.ARROW, 1).build());
    }

    @EventHandler
    public void execute(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow arrow
                && arrow.hasMetadata("ONE_SHOT")
                && event.getHitEntity() != null
                && event.getHitEntity() instanceof Player hit) {
            Player shooter = (Player) arrow.getShooter();

            hit.damage(hit.getHealth() * 2, shooter);

            event.getEntity().remove();
            event.getHitEntity().setLastDamageCause(new EntityDamageByEntityEvent(shooter, hit, EntityDamageEvent.DamageCause.PROJECTILE, hit.getHealth()));
        }
    }

    @EventHandler
    public void execute(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        if (this.plugin.gameStateManager.currentGameState().getGameStatePhase() != Types.INGAME) return;

        if (event.getBow().getType() == Material.CROSSBOW
                && event.getBow().getItemMeta() != null
                && event.getBow().getItemMeta().getDisplayName().equalsIgnoreCase(this.getName()))
            this.plugin.setMetadata(event.getProjectile(), "ONE_SHOT", true);
    }
}