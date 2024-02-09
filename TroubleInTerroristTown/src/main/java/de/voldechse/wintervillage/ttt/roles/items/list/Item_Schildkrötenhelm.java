package de.voldechse.wintervillage.ttt.roles.items.list;

import de.voldechse.wintervillage.library.util.ItemBuilder;
import de.voldechse.wintervillage.ttt.TTT;
import de.voldechse.wintervillage.ttt.gamestate.Types;
import de.voldechse.wintervillage.ttt.roles.items.RoleItem;
import eu.cloudnetservice.driver.inject.InjectionLayer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

public class Item_Schildkrötenhelm extends RoleItem implements Listener {

    private final TTT plugin = InjectionLayer.ext().instance(TTT.class);

    @Override
    public String getName() {
        //return "§2Schildkrötenhelm";
        return "§2Santas Hat";
    }

    @Override
    public ItemStack getIcon(boolean traitor) {
        return new ItemBuilder(Material.TURTLE_HELMET, 1, this.getName())
                .lore("",
                        "§7Dieser Helm reflektiert 50 % Schaden von Pfeilen",
                        "",
                        "§ePreis: " + (traitor ? "§c" : "§a") + this.getNeededPoints() + " Punkte",
                        "",
                        "§a<Klicke zum Kaufen>")
                .build();
    }

    @Override
    public void equipItems(Player player) {
        player.getInventory().setHelmet(new ItemBuilder(Material.TURTLE_HELMET, 1, this.getName())
                .lore("",
                        "§7Dieser Helm reflektiert 50 % Schaden von Pfeilen")
                .build());
    }

    @Override
    public int howOftenBuyable() {
        return 2;
    }

    @Override
    public boolean alradyInInventory(Player player) {
        return player.getInventory().getBoots() != null && player.getInventory().getBoots().getType() == Material.TURTLE_HELMET;
    }

    @Override
    public int getNeededPoints() {
        return 3;
    }

    @EventHandler
    public void execute(PlayerItemDamageEvent event) {
        if (event.getItem().getType() == Material.TURTLE_HELMET
                && event.getItem().getItemMeta().getDisplayName().equalsIgnoreCase(this.getName()))
            event.setDamage(0);
    }

    @EventHandler
    public void execute(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player receiver && event.getDamager() instanceof Arrow) {
            if (this.plugin.gameStateManager.currentGameState().getGameStatePhase() != Types.INGAME) {
                event.setCancelled(true);
                return;
            }

            if (this.plugin.gameManager.isSpectator(receiver)) {
                event.setCancelled(true);
                return;
            }

            Entity damager = event.getDamager();

            if (receiver.getInventory().getHelmet() != null
                    && receiver.getInventory().getHelmet().getItemMeta().getDisplayName().equalsIgnoreCase(this.getName())
                    && damager instanceof Arrow) {
                if (((Arrow) damager).getShooter() == null) return;
                Player shooter = (Player) ((Arrow) damager).getShooter();

                ItemStack helmet = receiver.getInventory().getHelmet();
                short newDurability = (short) (helmet.getDurability() + 0.1 * helmet.getType().getMaxDurability());
                helmet.setDurability(newDurability);

                double reflectedDamage = event.getDamage();

                shooter.playSound(shooter.getLocation(), Sound.ENCHANT_THORNS_HIT, 1.0f, 1.0f);
                shooter.damage(reflectedDamage * .5);

                this.plugin.setMetadata(shooter, "LAST_DAMAGER", receiver.getUniqueId());
            }
        }
    }
}